package me.miki.shindo.management.music.ytdlp;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.OSUtils;

import java.io.File;
import java.io.IOException;


public class YTDLP {

    public boolean download(String url) {
        try {
            FileManager fileManager = Shindo.getInstance().getFileManager();


            // ✅ Nome do executável do yt-dlp
            String ytName = OSUtils.isWindows() ? "yt-dlp.exe" : "yt-dlp";

            // 📂 Caminhos do yt-dlp e ffmpeg
            File ytdlpFile = new File(fileManager.getExternalDir(), "ytdlp/" + ytName);
            File ffmpegDir = new File(fileManager.getExternalDir(), "ffmpeg");

            // 📂 No Windows: ffmpeg/bin | No Linux/macOS: ffmpeg extraído na raiz
            File ffmpegLocation = new File(ffmpegDir, "bin");

            // ✅ Comando yt-dlp
            String[] command = {
                    ytdlpFile.getAbsolutePath(),
                    "-x",
                    "--embed-thumbnail",
                    "--audio-format", "mp3",
                    "--ffmpeg-location", ffmpegLocation.getAbsolutePath(),
                    "-f", "bestaudio",
                    "-o", fileManager.getMusicDir().getAbsolutePath() + "/%(title)s.%(ext)s",
                    url
            };

            ProcessBuilder pb = new ProcessBuilder(command);

            // ✅ Herda a saída de yt-dlp para o console do client
            pb.inheritIO();

            // ▶️ Executa
            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}