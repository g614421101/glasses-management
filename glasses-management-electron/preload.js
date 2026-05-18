const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    onUpdateStatus: (callback) => ipcRenderer.on('update-status', (_event, text, progress) => callback(text, progress)),
    getLogPath: () => ipcRenderer.invoke('get-log-path'),
    openLogFolder: () => ipcRenderer.invoke('open-log-folder'),
});
