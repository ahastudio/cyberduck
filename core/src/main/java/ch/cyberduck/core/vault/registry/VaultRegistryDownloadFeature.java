package ch.cyberduck.core.vault.registry;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryDownloadFeature implements Download {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final Download proxy;

    public VaultRegistryDownloadFeature(final Session<?> session, final Download proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        registry.find(file).getFeature(session, Download.class, proxy).download(file, local, throttle, listener, status, callback);
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return registry.find(file).getFeature(session, Download.class, proxy).offset(file);
    }
}