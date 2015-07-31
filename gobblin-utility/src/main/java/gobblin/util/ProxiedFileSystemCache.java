/*
* Copyright (C) 2014-2015 LinkedIn Corp. All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use
* this file except in compliance with the License. You may obtain a copy of the
* License at  http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed
* under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
* CONDITIONS OF ANY KIND, either express or implied.
*/

package gobblin.util;

import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.NonNull;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.token.Token;

import gobblin.configuration.ConfigurationKeys;
import gobblin.configuration.State;


/**
 * A cache for storing a mapping between Hadoop users and user {@link FileSystem} objects.
 *
 * <p>
 *  This classes uses Guava's {@link Cache} for storing the user to {@link FileSystem} mapping, and creates the
 *  {@link FileSystem}s using the {@link ProxiedFileSystemUtils} class.
 * </p>
 *
 * @see {@link Cache}, {@link ProxiedFileSystemUtils}
 */
public class ProxiedFileSystemCache {

  private static final int DEFAULT_MAX_CACHE_SIZE = 1000;

  private static final Cache<String, FileSystem> USER_NAME_TO_FILESYSTEM_CACHE = CacheBuilder.newBuilder()
      .maximumSize(DEFAULT_MAX_CACHE_SIZE).build();

  /**
   * Cached version of {@link ProxiedFileSystemUtils#getProxiedFileSystemUsingKeytab(String, String, Path, URI, Configuration)}.
   */
  public static FileSystem getProxiedFileSystemUsingKeytab(@NonNull final String userNameToProxyAs,
      final String superUserName, final Path superUserKeytabLocation, final URI fsURI, final Configuration conf)
      throws ExecutionException {

    return USER_NAME_TO_FILESYSTEM_CACHE.get(userNameToProxyAs, new Callable<FileSystem>() {
      @Override
      public FileSystem call() throws Exception {
        return ProxiedFileSystemUtils.getProxiedFileSystemUsingKeytab(userNameToProxyAs, superUserName,
            superUserKeytabLocation, fsURI, conf);
      }
    });
  }

  /**
   * Cached version of {@link ProxiedFileSystemUtils#getProxiedFileSystemUsingKeytab(State, URI, Configuration)}.
   */
  public static FileSystem getProxiedFileSystemUsingKeytab(State state, URI fsURI, Configuration conf)
      throws ExecutionException {
    Preconditions.checkArgument(state.contains(ConfigurationKeys.FS_PROXY_AS_USER_NAME));
    Preconditions.checkArgument(state.contains(ConfigurationKeys.SUPER_USER_NAME_TO_PROXY_AS_OTHERS));
    Preconditions.checkArgument(state.contains(ConfigurationKeys.SUPER_USER_KEY_TAB_LOCATION));

    return getProxiedFileSystemUsingKeytab(state.getProp(ConfigurationKeys.FS_PROXY_AS_USER_NAME),
        state.getProp(ConfigurationKeys.SUPER_USER_NAME_TO_PROXY_AS_OTHERS),
        new Path(state.getProp(ConfigurationKeys.SUPER_USER_KEY_TAB_LOCATION)), fsURI, conf);
  }

  /**
   * Cached version of {@link ProxiedFileSystemUtils#getProxiedFileSystemUsingToken(String, Token, URI, Configuration)}.
   */
  public static FileSystem getProxiedFileSystemUsingToken(@NonNull final String userNameToProxyAs,
      final Token<?> userNameToken, final URI fsURI, final Configuration conf) throws ExecutionException {

    return USER_NAME_TO_FILESYSTEM_CACHE.get(userNameToProxyAs, new Callable<FileSystem>() {
      @Override
      public FileSystem call() throws Exception {
        return ProxiedFileSystemUtils.getProxiedFileSystemUsingToken(userNameToProxyAs, userNameToken, fsURI, conf);
      }
    });
  }
}