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
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public interface IFileHandler {

  @Nullable FileInputStream openForReading(String path) throws IOException;

  @Nullable FileOutputStream openForWriting(String path) throws IOException;

  String getAbsolutePath(String path);

  boolean doesFileExist(String path);

  void saveResource(String path);

  /**
   * Create all directories specified by the path, which includes the base directory
   * as well as all of it's parents
   * @return {@link ETriResult#SUCCESS} on success, {@link ETriResult#EMPTY} if the
   *         directories already existed and {@link ETriResult#ERROR} if at least one
   *         directory could not be created (was already a file, missing permissions, etc)
   */
  ETriResult makeDirectories(String path);

}
