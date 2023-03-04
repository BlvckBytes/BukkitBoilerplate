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

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ConsoleSenderLogger implements ILogger {

  private final Plugin plugin;
  private boolean enableDebug;

  public ConsoleSenderLogger(Plugin plugin) {
    this.plugin = plugin;
  }

  private String buildLogPrefix(ELogLevel level) {
    String result = "[" + this.plugin.getName() + "] ";

    // Don't add a useless level prefix on info
    if (level == ELogLevel.INFO)
      return result;

    return result + "[" + level.color + level.name() + "§r] ";
  }

  @Override
  public void log(ELogLevel level, String message) {
    if (level == ELogLevel.DEBUG && !enableDebug)
      return;

    String[] lines = message.split("\n");

    for (String line : lines)
      Bukkit.getConsoleSender().sendMessage(buildLogPrefix(level) + line);
  }

  @Override
  public void logError(Throwable error) {
    try (
      StringWriter sWriter = new StringWriter();
      PrintWriter pWriter = new PrintWriter(sWriter)
    ) {
      error.printStackTrace(pWriter);
      log(ELogLevel.ERROR, sWriter.toString());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setEnableDebug(boolean enable) {
    this.enableDebug = enable;
  }
}
