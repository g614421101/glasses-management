const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');
const { pathToFileURL } = require('url');
const { spawn, exec } = require('child_process');
const fs = require('fs');

// ── Logging ────────────────────────────────────────────────────────────────
let logDir = null;
let logFilePath = null;
let logStream = null;

function initLogger() {
    // Ensure logs directory under userData
    logDir = path.join(app.getPath('userData'), 'logs');
    if (!fs.existsSync(logDir)) {
        fs.mkdirSync(logDir, { recursive: true });
    }

    // One log file per session, timestamped
    const now = new Date();
    const ts = [
        now.getFullYear(),
        String(now.getMonth() + 1).padStart(2, '0'),
        String(now.getDate()).padStart(2, '0'),
        '-',
        String(now.getHours()).padStart(2, '0'),
        String(now.getMinutes()).padStart(2, '0'),
        String(now.getSeconds()).padStart(2, '0'),
    ].join('');
    logFilePath = path.join(logDir, `backend-${ts}.log`);
    logStream = fs.createWriteStream(logFilePath, { flags: 'a', encoding: 'utf-8' });
}

function logToFile(level, message) {
    const now = new Date().toISOString();
    const line = `[${now}] [${level}] ${message}\n`;
    if (logStream) {
        logStream.write(line);
    }
}

function logInfo(msg) {
    console.log(msg);
    logToFile('INFO', msg);
}

function logError(msg) {
    console.error(msg);
    logToFile('ERROR', msg);
}

function getLogFilePath() {
    return logFilePath;
}

// ── Globals ─────────────────────────────────────────────────────────────────
let mainWindow;
let javaProcess = null;

// ── IPC: expose log path to renderer ────────────────────────────────────────
function registerIpc() {
    ipcMain.handle('get-log-path', () => getLogFilePath());
    ipcMain.handle('open-log-folder', () => {
        const p = getLogFilePath();
        if (p && fs.existsSync(p)) {
            shell.openPath(path.dirname(p));
            return true;
        }
        return false;
    });
}

// ── Create Window ───────────────────────────────────────────────────────────
async function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1200,
        height: 800,
        show: false,
        autoHideMenuBar: true,
        webPreferences: {
            nodeIntegration: false,
            contextIsolation: true,
            preload: path.join(__dirname, 'preload.js')
        }
    });

    mainWindow.loadFile('loading.html');
    mainWindow.show();

    // Step 1: Diagnostic info
    dumpDiagnostics();

    // Step 2: Ensure Java Environment
    let javaPath;
    try {
        javaPath = await ensureJavaEnvironment();
    } catch (e) {
        const msg = `环境初始化失败: ${e.message}`;
        logError(msg);
        mainWindow.webContents.send('update-status', msg);
        return;
    }

    // Step 3: Start Backend
    mainWindow.webContents.send('update-status', '启动后台引擎中...');
    const backendStarted = startBackend(javaPath);
    if (!backendStarted) {
        return; // UI is already updated with error
    }

    // Step 4: Wait for Backend
    mainWindow.webContents.send('update-status', '建立本地连接中...');
    try {
        await waitForBackend('http://127.0.0.1:8080', 60000);
        // Step 5: Load App
        mainWindow.loadURL('http://127.0.0.1:8080');
    } catch (err) {
        const msg = `服务连接超时，请检查日志文件获取详情:\n${getLogFilePath()}`;
        logError(msg);
        mainWindow.webContents.send('update-status', msg);
    }
}

// ── Diagnostics ─────────────────────────────────────────────────────────────
function dumpDiagnostics() {
    logInfo('========== 启动诊断信息 ==========');
    logInfo(`应用版本: ${app.getVersion()}`);
    logInfo(`是否打包: ${app.isPackaged}`);
    logInfo(`应用路径: ${app.getAppPath()}`);
    logInfo(`用户数据路径: ${app.getPath('userData')}`);
    logInfo(`资源路径: ${process.resourcesPath || '(未设置)'}`);
    logInfo(`临时目录: ${app.getPath('temp')}`);
    logInfo(`进程架构: ${process.arch}`);
    logInfo(`平台: ${process.platform}`);
    logInfo(`Node 版本: ${process.version}`);
    logInfo(`当前工作目录: ${process.cwd()}`);
    logInfo(`系统环境变量 PATH: ${process.env.PATH || '(无)'}`);
    logInfo(`JAVA_HOME: ${process.env.JAVA_HOME || '(未设置)'}`);
    logInfo('==================================');
}

// ── Java Discovery ──────────────────────────────────────────────────────────
function findBundledJava() {
    const candidates = [];

    if (app.isPackaged) {
        candidates.push(path.join(process.resourcesPath, 'runtime', 'jre', 'bin', 'java.exe'));
    }

    candidates.push(path.join(__dirname, 'runtime', 'jre', 'bin', 'java.exe'));

    for (const candidate of candidates) {
        logInfo(`检查 Java 路径: ${candidate}  ->  ${fs.existsSync(candidate) ? '存在' : '不存在'}`);
        if (fs.existsSync(candidate)) {
            return candidate;
        }
    }

    return null;
}

async function ensureJavaEnvironment() {
    const bundledJava = findBundledJava();
    if (bundledJava) {
        logInfo(`使用内置 Java: ${bundledJava}`);
        mainWindow.webContents.send('update-status', '载入内置运行环境，准备启动...');
        return bundledJava;
    }

    const msg = app.isPackaged
        ? '未找到内置 Java 运行环境，请重新安装当前版本应用'
        : '未找到 runtime/jre/bin/java.exe，请先执行打包脚本生成内置运行环境';
    logError(msg);
    throw new Error(msg);
}

// ── Backend Launch ──────────────────────────────────────────────────────────
function startBackend(javaCommand) {
    let jarPath = path.join(__dirname, '..', 'glasses-management-backend-h2', 'target');
    let finalJar = null;
    let backendDir = null;

    if (!app.isPackaged) {
        // Dev mode
        if (fs.existsSync(jarPath)) {
            const files = fs.readdirSync(jarPath).filter(f => f.endsWith('.jar') && !f.endsWith('.original'));
            if (files.length > 0) finalJar = path.join(jarPath, files[0]);
            backendDir = path.join(__dirname, '..', 'glasses-management-backend-h2');
        }
    } else {
        // Prod mode
        backendDir = path.join(process.resourcesPath, 'backend');
        logInfo(`查找 JAR 目录: ${backendDir}  ->  ${fs.existsSync(backendDir) ? '存在' : '不存在'}`);
        if (fs.existsSync(backendDir)) {
            const files = fs.readdirSync(backendDir).filter(f => f.endsWith('.jar'));
            logInfo(`JAR 候选文件: ${JSON.stringify(files)}`);
            if (files.length > 0) finalJar = path.join(backendDir, files[0]);
        }
    }

    if (!finalJar) {
        const msg = '获取后台组件失败: 未在对应路径找到 JAR 文件';
        logError(msg);
        mainWindow.webContents.send('update-status', msg);
        return false;
    }

    logInfo(`最终 JAR: ${finalJar}`);

    const configLocations = resolveConfigLocations(backendDir);
    logInfo(`外部配置文件: ${configLocations || '(无)'}`);

    // Build JVM arguments
    const userDataPath = app.getPath('userData');
    const javaArgs = [
        `-Dapp.home=${userDataPath}`,              // so Spring logging file.path can use ${app.home}
        `-Dfile.encoding=UTF-8`,
        '-jar', finalJar,
        '--app.browser.auto-launch=false',
    ];
    if (configLocations) {
        javaArgs.push(`--spring.config.additional-location=${configLocations}`);
    }

    const spawnOptions = {
        cwd: userDataPath,
        windowsHide: true,   // don't flash a console window
    };

    logInfo(`启动命令: ${javaCommand} ${javaArgs.join(' ')}`);
    logInfo(`工作目录 (cwd): ${userDataPath}`);

    javaProcess = spawn(javaCommand, javaArgs, spawnOptions);

    // ── Capture stdout ──
    javaProcess.stdout.on('data', (data) => {
        const text = data.toString();
        process.stdout.write(text);        // still show in dev console
        logToFile('STDOUT', text.trimEnd());
    });

    // ── Capture stderr ──
    let stderrBuffer = '';
    javaProcess.stderr.on('data', (data) => {
        const text = data.toString();
        process.stderr.write(text);
        logToFile('STDERR', text.trimEnd());
        stderrBuffer += text;
    });

    // ── Handle spawn error (JVM binary can't run at all) ──
    javaProcess.on('error', (err) => {
        const msg = `无法启动 Java 进程: ${err.message}\n请检查是否缺少 VC++ 运行库，或内置 JRE 是否与当前系统架构匹配。`;
        logError(msg);
        logError(`错误码: ${err.code || '无'}  errno: ${err.errno || '无'}`);
        mainWindow.webContents.send('update-status', msg + `\n\n日志文件: ${getLogFilePath()}`);
    });

    // ── Handle exit ──
    javaProcess.on('exit', (code, signal) => {
        logInfo(`Java 进程退出 — 退出码: ${code}, 信号: ${signal || '无'}`);
        if (code !== 0) {
            const tail = stderrBuffer.length > 2000
                ? stderrBuffer.slice(-2000)
                : stderrBuffer;
            const errMsg = `后台组件异常退出！\n`
                + `退出码: ${code}  信号: ${signal || '无'}\n`
                + `日志文件: ${getLogFilePath()}\n`
                + `\n最近 stderr 输出 (最后 2000 字符):\n${tail || '(无输出)'}`;
            logError(errMsg);
            mainWindow.webContents.send('update-status', errMsg);
        }
    });

    return true;
}

function resolveConfigLocations(backendDir) {
    const candidates = [];

    if (backendDir) {
        const cfgInBackendDir = path.join(backendDir, 'application-local.yml');
        candidates.push(cfgInBackendDir);
        logInfo(`候选配置 1: ${cfgInBackendDir}  ->  ${fs.existsSync(cfgInBackendDir) ? '存在' : '不存在'}`);
    }

    const cfgInUserData = path.join(app.getPath('userData'), 'application-local.yml');
    candidates.push(cfgInUserData);
    logInfo(`候选配置 2: ${cfgInUserData}  ->  ${fs.existsSync(cfgInUserData) ? '存在' : '不存在'}`);

    const existing = candidates
        .filter(candidate => candidate && fs.existsSync(candidate))
        .map(candidate => `optional:${pathToFileURL(candidate).href}`);

    return existing.join(',');
}

// ── Backend Health Check ────────────────────────────────────────────────────
function waitForBackend(url, timeoutMs) {
    return new Promise((resolve, reject) => {
        const start = Date.now();
        const check = () => {
            const req = require('http').get(url, (res) => {
                if (res.statusCode >= 200 && res.statusCode < 500) {
                    resolve();
                } else {
                    retry();
                }
            }).on('error', () => {
                retry();
            });
            req.end();
        };

        const retry = () => {
            if (Date.now() - start > timeoutMs) {
                logError('等待后台服务连接超时 (60s)');
                reject(new Error('等待后台服务连接超时'));
            } else {
                setTimeout(check, 1000);
            }
        };

        check();
    });
}

// ── App Lifecycle ───────────────────────────────────────────────────────────
app.whenReady().then(() => {
    initLogger();
    registerIpc();
    logInfo('========== 应用启动 ==========');
    createWindow();
});

app.on('window-all-closed', () => {
    logInfo('所有窗口已关闭，正在退出...');
    if (javaProcess) {
        logInfo(`清理 Java 进程 PID: ${javaProcess.pid}`);
        exec(`taskkill /pid ${javaProcess.pid} /t /f`, (err) => {
            if (err) logError(`清理 Java 进程失败: ${err.message}`);
            logInfo('========== 应用退出 ==========');
            if (logStream) logStream.end();
            app.quit();
        });
    } else {
        logInfo('========== 应用退出 ==========');
        if (logStream) logStream.end();
        app.quit();
    }
});
