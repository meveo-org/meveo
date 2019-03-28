/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.api.technicalservice.endpoint;

import org.meveo.service.script.Script;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class TimedOutScript extends Script {

    private int counter;
    private CompletableFuture future;

    @Override
    public void execute(Map<String, Object> methodContext) {
        future = CompletableFuture.runAsync(() -> {
            while (true) {
                counter++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException | CancellationException e) {
                    break;
                }
            }
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException ignored) {

        }

    }

    @Override
    public Map<String, Object> cancel() {
        future.cancel(true);
        Map<String, Object> result = new HashMap<>();
        result.put("counter", counter);
        return result;
    }
}
