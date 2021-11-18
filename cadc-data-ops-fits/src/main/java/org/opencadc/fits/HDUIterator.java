/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2021.                            (c) 2021.
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
 *
 ************************************************************************
 */

package org.opencadc.fits;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;


/**
 * Iterate over HDUs of a nom.tam.fits.Fits object.  The Fits object essentially contains two lists of HDUs; one is the
 * cache of HDUs that have been iterated over, and the second is the list of HDUs that have yet to be read.  This
 * iterator simplifies that by first iterating the cache, then it reads the unread HDUs.
 * This is a read-only operation, so the remove() operation is not supported.
 */
public class HDUIterator implements Iterator<BasicHDU<?>> {

    private final Iterator<BasicHDU<?>> unCachedIterator;
    private Iterator<BasicHDU<?>> currentIterator;


    public HDUIterator(final Fits source) {
        currentIterator = new Iterator<BasicHDU<?>>() {

            private final int cachedHDUCount = source.getNumberOfHDUs();
            private int currentHDUIndex = 0;

            @Override
            public boolean hasNext() {
                return currentHDUIndex < cachedHDUCount;
            }

            @Override
            public BasicHDU<?> next() {
                try {
                    final BasicHDU<?> nextHDU = source.getHDU(currentHDUIndex);

                    if (nextHDU == null) {
                        throw new NoSuchElementException("No more elements.");
                    } else {
                        currentHDUIndex++;
                        return nextHDU;
                    }
                } catch (FitsException | IOException exception) {
                    throw new IllegalStateException(exception.getMessage(), exception);
                }
            }
        };

        unCachedIterator = new Iterator<BasicHDU<?>>() {

            private BasicHDU<?> currentHDU = null;

            @Override
            public boolean hasNext() {
                try {
                    return currentHDU != null || (currentHDU = source.readHDU()) != null;
                } catch (FitsException | IOException exception) {
                    throw new IllegalStateException(exception.getMessage(), exception);
                }
            }

            @Override
            public BasicHDU<?> next() {
                try {
                    final BasicHDU<?> nextHDU = currentHDU == null ? source.readHDU() : currentHDU;

                    if (nextHDU == null) {
                        throw new NoSuchElementException("No more elements.");
                    } else {
                        return nextHDU;
                    }
                } catch (FitsException | IOException exception) {
                    throw new IllegalStateException(exception.getMessage(), exception);
                } finally {
                    currentHDU = null;
                }
            }
        };
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        if (!currentIterator.hasNext()) {
            currentIterator = unCachedIterator;
        }

        return currentIterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public BasicHDU<?> next() {
        return currentIterator.next();
    }
}
