// Copyright 2004 Elliotte Rusty Harold
// 
// This library is free software; you can redistribute 
// it and/or modify it under the terms of version 2.1 of 
// the GNU Lesser General Public License as published by  
// the Free Software Foundation.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
// GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General 
// Public License along with this library; if not, write to the 
// Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
// Boston, MA  02111-1307  USA
// 
// You can contact Elliotte Rusty Harold by sending e-mail to
// elharo@metalab.unc.edu. Please include the word "XOM" in the
// subject line. The XOM home page is temporarily located at
// http://www.cafeconleche.org/XOM/  but will eventually move
// to http://www.xom.nu/
package nu.xom;

import com.sun.org.apache.xerces.internal.parsers.SAXParser;
import com.sun.org.apache.xerces.internal.parsers.DTDConfiguration;

/**
 * <p>
 * This class is used by the <code>Builder</code> to prevent the
 * repackaged Xerces shipped with Java 1.5 and some of the JAXP
 * reference implementations from accepting XML 1.1 documents. 
 * This is packaged and built separately from the normal classes to 
 * enable Java 1.4 and earlier compilers to handle this.  
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.0b1
 * 
 */
class JDK15XML1_0Parser extends SAXParser {

    JDK15XML1_0Parser() {
        super(new DTDConfiguration());
    }

}
