package me.miki.shindo.management.remote.download;

import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.remote.download.file.DownloadFile;
import me.miki.shindo.management.remote.download.file.DownloadZipFile;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.OSUtils;
import me.miki.shindo.utils.file.FileUtils;
import me.miki.shindo.utils.network.HttpUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


public class DownloadManager {

    private final ArrayList<DownloadFile> downloadFiles = new ArrayList<>();
    private boolean downloaded;

    public DownloadManager() {
        FileManager fileManager = Shindo.getInstance().getFileManager();
        downloaded = false;

        // ✅ Windows
        if (OSUtils.isWindows()) {
            // yt-dlp
            downloadFiles.add(new DownloadFile(
                    "https://github.com/yt-dlp/yt-dlp/releases/download/2025.07.21/yt-dlp.exe",
                    "yt-dlp.exe",
                    new File(fileManager.getExternalDir(), "ytdlp"),
                    -1));

            // ffmpeg → salva sempre como ffmpeg.zip
            downloadFiles.add(new DownloadZipFile(
                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip",
                    "ffmpeg-master-latest-win64-gpl.zip",
                    new File(fileManager.getExternalDir(), "ffmpeg"),
                    -1, -1));

            // ✅ Linux
        } else if (OSUtils.isLinux()) {
            downloadFiles.add(new DownloadFile(
                    "https://github.com/yt-dlp/yt-dlp/releases/download/2025.07.21/yt-dlp",
                    "yt-dlp",
                    new File(fileManager.getExternalDir(), "ytdlp"),
                    -1));

            // ffmpeg → salva sempre como ffmpeg.tar.xz
            downloadFiles.add(new DownloadZipFile(
                    "https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz",
                    "ffmpeg-release-amd64-static.tar.xz",
                    new File(fileManager.getExternalDir(), "ffmpeg"),
                    -1, -1));

            // ✅ macOS
        } else if (OSUtils.isMac()) {
            downloadFiles.add(new DownloadFile(
                    "https://github.com/yt-dlp/yt-dlp/releases/download/2025.07.21/yt-dlp",
                    "yt-dlp",
                    new File(fileManager.getExternalDir(), "ytdlp"),
                    -1));

            downloadFiles.add(new DownloadZipFile(
                    "https://evermeet.cx/ffmpeg/ffmpeg-7.1.1.zip",
                    "ffmpeg-7.1.1.zip",
                    new File(fileManager.getExternalDir(), "ffmpeg"),
                    -1, -1));
        }

        // Roda o download de forma assíncrona
        Multithreading.runAsync(this::startDownloads);
    }

    private void startDownloads() {
        for (DownloadFile df : downloadFiles) {

            if (!df.getOutputDir().exists()) {
                df.getOutputDir().mkdirs();
            }

            // ✅ Se for arquivo compactado (zip/tar.xz)
            if (df instanceof DownloadZipFile) {
                DownloadZipFile dzf = (DownloadZipFile) df;

                // Se pasta não tem conteúdo, baixa e extrai
                if (FileUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {
                    File outputFile = new File(dzf.getOutputDir(), dzf.getFileName());
                    ShindoLogger.info("Baixando: " + dzf.getUrl());
                    ShindoLogger.info("Salvando em: " + outputFile.getAbsolutePath());

                    HttpUtils.downloadFile(dzf.getUrl(), outputFile, dzf.getSize());

                    // ✅ Verifica se o download completou (arquivo não está vazio)
                    if (outputFile.length() > 0) {
                        FileUtils.extract(outputFile, dzf.getOutputDir());

                        if (dzf.getFileName().contains("ffmpeg")) {
                            fixFfmpegStructure(dzf.getOutputDir());
                        }

                        outputFile.delete();
                    } else {
                        ShindoLogger.error("Download de " + outputFile.getName() + " falhou, arquivo vazio.");
                    }
                }

                // ✅ Se for arquivo normal (yt-dlp)
            } else {
                File outputFile = new File(df.getOutputDir(), df.getFileName());

                // Baixa se não existe ou se tamanho for diferente (quando size > 0)
                if (df.getSize() <= 0 || outputFile.length() != df.getSize()) {
                    ShindoLogger.info("Baixando: " + df.getUrl());
                    HttpUtils.downloadFile(df.getUrl(), outputFile, df.getSize());
                }
            }
        }

        // ✅ Dá permissão de execução no Linux/macOS
        if (!OSUtils.isWindows()) {
            try {
                FileManager fileManager = Shindo.getInstance().getFileManager();

                // 🔹 Dar permissão para yt-dlp (qualquer nome)
                File ytdlpDir = new File(fileManager.getExternalDir(), "ytdlp");
                File[] ytdlpFiles = ytdlpDir.listFiles();
                if (ytdlpFiles != null) {
                    for (File file : ytdlpFiles) {
                        file.setExecutable(true);
                        ShindoLogger.info(file.getName() + " marcado como executável.");
                    }
                }

                // 🔹 Dar permissão para todos os executáveis do FFmpeg
                File ffmpegBin = new File(fileManager.getExternalDir(), "ffmpeg/bin");
                File[] executables = ffmpegBin.listFiles();
                if (executables != null) {
                    for (File exe : executables) {
                        exe.setExecutable(true);
                        ShindoLogger.info(exe.getName() + " marcado como executável.");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                ShindoLogger.error("Falha ao dar permissão de execução: " + e.getMessage());
            }
        }

        checkFiles();
    }

    private void checkFiles() {
        for (DownloadFile df : downloadFiles) {
            if (df instanceof DownloadZipFile) {
                DownloadZipFile dzf = (DownloadZipFile) df;

                // ✅ Só checa se unzippedSize for maior que 0
                if (dzf.getUnzippedSize() > 0 && FileUtils.getDirectorySize(dzf.getOutputDir()) != dzf.getUnzippedSize()) {
                    startDownloads();
                }

            } else {
                File outputFile = new File(df.getOutputDir(), df.getFileName());

                // ✅ Só checa se size for maior que 0
                if (df.getSize() > 0 && outputFile.length() != df.getSize()) {
                    startDownloads();
                }
            }
        }

        downloaded = true;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    private void fixFfmpegStructure(File ffmpegRoot) {
        File binDir = new File(ffmpegRoot, "bin");

        // ✅ Se já tem bin, não faz nada
        if (binDir.exists()) {
            ShindoLogger.info("Estrutura do FFMPEG já corrigida, pasta bin já existe.");
            return;
        }

        File[] children = ffmpegRoot.listFiles();
        if (children == null) return;

        // ✅ Cria pasta bin
        binDir.mkdirs();

        for (File child : children) {
            if (child.isDirectory()) {
                // Procura executáveis em qualquer subpasta
                moveExecutables(child, binDir);

                // Depois de mover tudo, remove pasta extra
                deleteDirectory(child);
            }
        }

        // ✅ Checa se conseguiu mover algo
        if (binDir.listFiles() == null || Objects.requireNonNull(binDir.listFiles()).length == 0) {
            ShindoLogger.warn("Nenhum executável do FFmpeg encontrado!");
        } else {
            ShindoLogger.info("Estrutura do FFMPEG corrigida e pastas extras removidas.");
        }
    }

    /**
     * ✅ Move ffmpeg, ffplay e ffprobe para a pasta bin/
     */
    private void moveExecutables(File source, File binDir) {
        File[] files = source.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                // Procura dentro de subpastas também
                moveExecutables(f, binDir);
            } else {
                String name = f.getName().toLowerCase();
                if (name.startsWith("ffmpeg") || name.startsWith("ffplay") || name.startsWith("ffprobe")) {
                    f.renameTo(new File(binDir, f.getName()));
                    System.out.println("[INFO] Movido: " + f.getName());
                }
            }
        }
    }

    /**
     * ✅ Deleta uma pasta e todo o conteúdo dentro dela.
     */
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }

    public DownloadFile getDownloadByFile(String fileName) {
        for (DownloadFile file : downloadFiles) {
            if (file.getFileName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    public long getTotalSize() {
        return downloadFiles.stream().mapToLong(DownloadFile::getSize).sum();
    }

    public long getTotalDownloaded() {
        return downloadFiles.stream().mapToLong(DownloadFile::getDownloadedBytes).sum();
    }

    public float getProgress() {
        long total = getTotalSize();
        if (total == 0) return 0f;
        return (float) getTotalDownloaded() / total;
    }
}