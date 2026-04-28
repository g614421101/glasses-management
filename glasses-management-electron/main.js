const { app, BrowserWindow } = require('electron');
const path = require('path');
const { pathToFileURL } = require('url');
const { spawn, exec } = require('child_process');
const fs = require('fs');

let mainWindow;
let javaProcess = null;

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
    
    // Step 1: Ensure Java Environment
    let javaPath;
    try {
        javaPath = await ensureJavaEnvironment();
    } catch (e) {
        mainWindow.webContents.send('update-status', `环境初始化失败: ${e.message}`);
        return;
    }

    // Step 2: Start Backend
    mainWindow.webContents.send('update-status', '启动后台引擎中...');
    const backendStarted = startBackend(javaPath);
    if (!backendStarted) {
        return; // UI is already updated with error
    }

    // Step 3: Wait for Backend
    mainWindow.webContents.send('update-status', '建立本地连接中...');
    try {
        await waitForBackend('http://127.0.0.1:8080', 60000);
        // Step 4: Load App
        mainWindow.loadURL('http://127.0.0.1:8080');
    } catch(err) {
        mainWindow.webContents.send('update-status', `服务连接超时，请检查控制台: ${err.message}`);
    }
}

function findBundledJava() {
    const candidates = [];

    if (app.isPackaged) {
        candidates.push(path.join(process.resourcesPath, 'runtime', 'jre', 'bin', 'java.exe'));
    }

    candidates.push(path.join(__dirname, 'runtime', 'jre', 'bin', 'java.exe'));

    for (const candidate of candidates) {
        if (fs.existsSync(candidate)) {
            return candidate;
        }
    }

    return null;
}

async function ensureJavaEnvironment() {
    const bundledJava = findBundledJava();
    if (bundledJava) {
        mainWindow.webContents.send('update-status', '载入内置运行环境，准备启动...');
        return bundledJava;
    }

    throw new Error(
        app.isPackaged
            ? '未找到内置 Java 运行环境，请重新安装当前版本应用'
            : '未找到 runtime/jre/bin/java.exe，请先执行打包脚本生成内置运行环境'
    );
}

function startBackend(javaCommand) {
    let jarPath = path.join(__dirname, '..', 'glasses-management-backend-h2', 'target');
    let finalJar = null;
    let backendDir = null;
    
    if (!app.isPackaged) {
        // Dev mode
        if(fs.existsSync(jarPath)){
             const files = fs.readdirSync(jarPath).filter(f => f.endsWith('.jar') && !f.endsWith('.original'));
             if (files.length > 0) finalJar = path.join(jarPath, files[0]);
             backendDir = path.join(__dirname, '..', 'glasses-management-backend-h2');
        }
    } else {
        // Prod mode
        backendDir = path.join(process.resourcesPath, 'backend');
        if (fs.existsSync(backendDir)) {
            const files = fs.readdirSync(backendDir).filter(f => f.endsWith('.jar'));
            if(files.length > 0) finalJar = path.join(backendDir, files[0]);
        }
    }

    if (!finalJar) {
        console.error("Jar file not found!");
        mainWindow.webContents.send('update-status', '获取后台组件失败: 未在对应路径找到 JAR 文件');
        return false;
    }

    console.log(`Starting: ${javaCommand} -jar ${finalJar}`);
    const configLocations = resolveConfigLocations(backendDir);
    const javaArgs = ['-jar', finalJar, '--app.browser.auto-launch=false'];
    if (configLocations) {
        javaArgs.push(`--spring.config.additional-location=${configLocations}`);
    }
    const spawnOptions = {
        cwd: app.getPath('userData')
    };
    javaProcess = spawn(javaCommand, javaArgs, spawnOptions);

    let errorLog = '';

    javaProcess.stdout.on('data', (data) => console.log(`${data}`));
    javaProcess.stderr.on('data', (data) => {
        console.error(`ERR: ${data}`);
        errorLog += data.toString() + '\\n';
    });
    
    javaProcess.on('exit', (code) => {
        if (code !== 0) {
            mainWindow.webContents.send('update-status', `后台组件异常退出！状态码: ${code}\\n错误详情:\\n${errorLog}`);
        }
    });

    return true;
}

function resolveConfigLocations(backendDir) {
    const candidates = [];

    if (backendDir) {
        candidates.push(path.join(backendDir, 'application-local.yml'));
    }

    candidates.push(path.join(app.getPath('userData'), 'application-local.yml'));

    const existing = candidates
        .filter(candidate => candidate && fs.existsSync(candidate))
        .map(candidate => `optional:${pathToFileURL(candidate).href}`);

    return existing.join(',');
}

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
                 reject(new Error("等待后台服务连接超时"));
             } else {
                 setTimeout(check, 1000);
             }
        };

        check();
    });
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
    if (javaProcess) {
        console.log("Cleaning up Java process...");
        exec(`taskkill /pid ${javaProcess.pid} /t /f `, (err) => {
             app.quit();
        });
    } else {
        app.quit();
    }
});
