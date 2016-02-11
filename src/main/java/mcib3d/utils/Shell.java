package mcib3d.utils;

import ij.*;
import java.io.*;
import java.lang.Process;

/**
 * Copyright (C) Thomas Boudier
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Interface avec le shell (lancement de commandes)
 *
 * @author thomas
 * @created 4 septembre 2003
 */
public class Shell {

    /**
     * Lancement de commandes
     *
     * @param cmd La commande
     * @param verbose Description of the Parameter
     */
    public String runCommand(String cmd, boolean verbose) {
        Process p;
        String rep = "";
        try {
            System.out.println(cmd);
            p = Runtime.getRuntime().exec(cmd);
            // Print command output onto standard output
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String str;
            while ((str = br.readLine()) != null) {
                rep = rep.concat(str);
                rep.concat("\n");
                if (verbose) {
                    IJ.log(str);
                }
            }
            is.close();

            // Wait for the process to complete.
            int status = -1;
            try {
                status = p.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        } catch (java.io.IOException e) {
            System.out.println("erreur " + e);
        }
        return rep;
    }

    public String runCommand(String cmd) {
        return runCommand(cmd, false);
    }

}
