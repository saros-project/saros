package de.fu_berlin.inf.dpp.misc.sound;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class SoundPlayer {

  private static final Logger LOG = Logger.getLogger(SoundPlayer.class);

  // 16 Bit, 44.1 kHz, 2 seconds
  private static final int BUFFER_SIZE = 2 * 44100 * 2;

  private SoundPlayer() {
    // NOP
  }

  /**
   * Plays the given file on the default audio system. This method blocks until the file has been
   * played.
   *
   * @param file the file to play
   */
  public static void playSound(final File file) {

    final AudioInputStream audioInputStream;

    try {
      audioInputStream = AudioSystem.getAudioInputStream(file);
    } catch (UnsupportedAudioFileException e) {
      LOG.warn("unsupported audio file: " + file.getName(), e);
      return;
    } catch (IOException e) {
      LOG.error("unable to read audio file:" + file.getName(), e);
      return;
    }

    final AudioFormat format = audioInputStream.getFormat();
    final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

    final SourceDataLine audioLine;

    try {
      audioLine = (SourceDataLine) AudioSystem.getLine(info);
      audioLine.open(format);
    } catch (LineUnavailableException e) {
      LOG.warn("no audioline available, could not play audio file: " + file.getName(), e);
      IOUtils.closeQuietly(audioInputStream);
      return;
    } catch (Exception e) {
      LOG.error("could not access sound system", e);
      IOUtils.closeQuietly(audioInputStream);
      return;
    }

    int read = 0;
    final byte[] buffer = new byte[BUFFER_SIZE];

    audioLine.start();

    try {
      while (read != -1) {
        read = audioInputStream.read(buffer, 0, buffer.length);
        if (read >= 0) audioLine.write(buffer, 0, read);
      }
    } catch (IOException e) {
      LOG.error("I/O error while streaming audio file: " + file.getName(), e);
      return;
    } finally {
      audioLine.drain();
      audioLine.close();
      IOUtils.closeQuietly(audioInputStream);
    }
  }
}
