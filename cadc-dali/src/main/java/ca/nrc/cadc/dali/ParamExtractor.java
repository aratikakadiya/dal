/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2019.                            (c) 2019.
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
*  $Revision: 5 $
*
************************************************************************
 */

package ca.nrc.cadc.dali;

import ca.nrc.cadc.util.CaseInsensitiveStringComparator;
import ca.nrc.cadc.uws.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Extract a list of query parameter-value pairs from a UWS job parameter list. This
 * implementation assumes parameter names are not case sensitive and ignores unknown
 * parameter names.
 *
 * @author pdowler
 */
public class ParamExtractor {

    private Set<String> names = new TreeSet<String>(new CaseInsensitiveStringComparator());

    public ParamExtractor(List<String> paramNames) {
        this.names.addAll(paramNames);
    }

    /**
     * Get a map of parameter name to value(s). This map includes params that match the configured
     * accept list.
     *
     * @param paramList
     * @return list of 1+ values or null
     */
    public Map<String, List<String>> getParameters(List<Parameter> paramList) {
        Map<String, List<String>> ret = new TreeMap<String, List<String>>(new CaseInsensitiveStringComparator());
        // init with empty value list for known params
        for (String n : names) {
            ret.put(n, new ArrayList<String>());
        }

        for (Parameter p : paramList) {
            if (names.contains(p.getName())) {
                String pname = p.getName();
                List<String> values = ret.get(pname);
                values.add(p.getValue());
            }
        }
        return ret;
    }
    
    /**
     * Get a map of parameter name to value(s). This map includes extra params that do not match the configured
     * accept list.
     *
     * @param paramList
     * @return list of 1+ values or null
     */
    public Map<String, List<String>> getExtraParameters(List<Parameter> paramList) {
        Map<String, List<String>> ret = new TreeMap<String, List<String>>(new CaseInsensitiveStringComparator());
        for (Parameter p : paramList) {
            if (!names.contains(p.getName())) {
                String pname = p.getName();
                List<String> values = ret.get(pname);
                if (values == null) {
                    values = new ArrayList<String>();
                    ret.put(pname, values);
                }
                values.add(p.getValue());
            }
        }
        return ret;
    }
}
