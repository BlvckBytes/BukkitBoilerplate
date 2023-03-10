/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.bukkitboilerplate;

import me.blvckbytes.utilitytypes.ETriResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class PluginFileHandler implements IFileHandler {

  private final JavaPlugin plugin;

  public PluginFileHandler(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public @Nullable FileInputStream openForReading(String path) throws IOException {
    File file = new File(plugin.getDataFolder(), path);

    if (!(file.exists() && file.isFile()))
      return null;

    return new FileInputStream(file);
  }

  @Override
  public @Nullable FileOutputStream openForWriting(String path) throws IOException {
    File file = new File(plugin.getDataFolder(), path);

    // Path is a directory, don't overwrite
    if (file.exists() && !file.isFile())
      return null;

    // Ensure parent folder existence
    if (!file.getParentFile().exists()) {
      if (!file.getParentFile().mkdirs())
        return null;
    }

    return new FileOutputStream(file);
  }

  @Override
  public String getAbsolutePath(String path) {
    return new File(plugin.getDataFolder(), path).getAbsolutePath();
  }

  @Override
  public boolean doesFileExist(String path) {
    File file = new File(plugin.getDataFolder(), path);
    return file.exists() && file.isFile();
  }

  @Override
  public void saveResource(String path) {
    this.plugin.saveResource(path, true);
  }

  @Override
  public @Nullable InputStream getResource(String path) {
    return this.plugin.getResource(path);
  }

  @Override
  public ETriResult makeDirectories(String path) {
    File file = new File(plugin.getDataFolder(), path);

    if (file.isDirectory())
      return ETriResult.EMPTY;

    if (file.mkdirs())
      return ETriResult.SUCCESS;

    return ETriResult.ERROR;
  }
}
