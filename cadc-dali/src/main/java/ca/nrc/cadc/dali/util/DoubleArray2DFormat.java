/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2009.                            (c) 2009.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.dali.util;

import java.util.Iterator;

/**
 * Formats and parses a double[].
 *
 */
public class DoubleArray2DFormat implements Format<double[][]> {

    private int[] arrayshape;

    public DoubleArray2DFormat(int[] arrayshape) {
        this.arrayshape = arrayshape;
    }

    /**
     * Takes an double[] and returns the standard String representation.
     * If the double[] is null an empty String is returned.
     *
     * @param object double[] to format.
     * @return String representation of the double[].
     */
    public String format(double[][] object) {
        if (object == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < object.length; i++) {
            for (int j = 0; j < object[i].length; j++) {
                sb.append(Double.toString(object[i][j]));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Takes a sequence of double values and returns the standard String
     * representation. If the iterator is null or empty an empty String is
     * returned.
     *
     * @param iter
     * @return
     */
    public String format(Iterator<Double> iter) {
        if (iter == null || !iter.hasNext()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            sb.append(iter.next().toString());
            sb.append(" ");
        }
        sb.trimToSize();
        return sb.toString();
    }

    /**
     * Parses a String to a double[].
     *
     * @param s the String to parse.
     * @return double[] value of the String.
     */
    public double[][] parse(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        } else {
            String[] tokens = s.split(" +");
            int n1 = arrayshape[0];
            //int n2 = arrayshape[1];
            //if (arrayshape[1] == -1) // variable
            int n2 = tokens.length / arrayshape[0];
            double[][] array = new double[n1][n2];
            int i = 0;
            int j = 0;
            for (int t = 0; t < tokens.length; t++) {
                array[i][j] = Double.parseDouble(tokens[t]);
                j++;
                if (j == n2) {
                    j = 0;
                    i++;
                }
            }
            return array;
        }
    }

}
