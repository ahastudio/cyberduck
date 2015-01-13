package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferProgress;
import ch.cyberduck.core.transfer.TransferSpeedometer;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @version $Id$
 */
public class TerminalStreamListener implements StreamListener {

    private final Ansi ansi = Ansi.ansi();

    private TransferSpeedometer meter;

    private Console console = new Console();

    /**
     * Progress bar fixed width in characters
     */
    private int width = 30;

    public TerminalStreamListener(final TransferSpeedometer meter) {
        this.meter = meter;
    }

    private void increment() {
        final TransferProgress progress = meter.getStatus();
        final BigDecimal fraction = new BigDecimal(progress.getTransferred())
                .divide(new BigDecimal(progress.getSize()), 1, RoundingMode.DOWN);
        console.printf("\r%s[", ansi
                .saveCursorPosition()
                .eraseLine(Ansi.Erase.ALL)
                .restoreCursorPosition().toString());
        int i = 0;
        for(; i <= (int) (fraction.doubleValue() * width); i++) {
            console.printf("\u25AE");
        }
        for(; i < width; i++) {
            console.printf(StringUtils.SPACE);
        }
        console.printf("] %s", progress.getProgress());
    }

    @Override
    public void recv(final long bytes) {
        this.increment();
    }

    @Override
    public void sent(final long bytes) {
        this.increment();
    }
}
