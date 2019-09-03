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

package org.meveo.jmeter.utils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Waiting {

    private final JProgressBar jProgressBar = new JProgressBar();
    private final JPanel panel = new JPanel();

    private Component componentToHide;
    private Window frame;
    private Future runningThread;

    public Waiting(Window window, Component componentToHide){
        this.componentToHide = componentToHide;
        this.frame = window;

        jProgressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        jProgressBar.setSize(Integer.MAX_VALUE, 50);
        jProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        panel.setLayout(new BorderLayout());
        panel.add(jProgressBar, BorderLayout.SOUTH);
    }

    public Waiting(JPanel panel, String position){
        this(panel, position, true);
    }

    public Waiting(JPanel panel, String position, boolean visible){
        panel.add(jProgressBar, position);
        jProgressBar.setVisible(visible);
    }

    public void start(){
        if(frame != null){
            frame.remove(componentToHide);
            frame.add(panel);
            panel.setVisible(true);
            frame.repaint();
            frame.revalidate();
            frame.pack();
        }else{
            jProgressBar.setVisible(true);
        }
        jProgressBar.setValue(0);
        runningThread = run();
    }

    public void stop(){
        runningThread.cancel(true);

        if(frame != null){
            frame.remove(panel);
            panel.setVisible(false);
            frame.add(componentToHide);
            frame.repaint();
            frame.revalidate();
            frame.pack();
        }else{
            jProgressBar.setVisible(false);
        }

    }


    private Future run(){
        ExecutorService service = Executors.newFixedThreadPool(1);
        return service.submit(() -> {
            int i = 0;
            while(true){
                i = (i + 1) % 100;
                if(Thread.interrupted()){
                    break;
                }else{
                    jProgressBar.setValue(i);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
    }

}
