// Copyright 2002, 2003 Elliotte Rusty Harold
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

package nu.xom.tests;

import nu.xom.Element;
import nu.xom.IllegalDataException;
import nu.xom.IllegalTargetException;
import nu.xom.ProcessingInstruction;

/**
 * <p>
 * Unit tests for the <code>ProcessingInstruction</code> class.
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.0d22
 *
 */
public class ProcessingInstructionTest extends XOMTestCase {

    public ProcessingInstructionTest() {
        super("Processing Instruction tests");
    }

    public ProcessingInstructionTest(String name) {
        super(name);
    }
    
    private ProcessingInstruction pi;
    
    protected void setUp() {
        pi = new ProcessingInstruction("test", "test");  
    }

    public void testToXML() {
        assertEquals("<?test test?>", pi.toXML());
    }

    public void testToString() {
        assertEquals(
          "[nu.xom.ProcessingInstruction: target=\"test\"; data=\"test\"]", 
          pi.toString());
    }

    public void testConstructor() {

        assertEquals("test", pi.getValue());
        assertEquals("test", pi.getTarget());

        try {
          new ProcessingInstruction("test:test", "test");
          fail("Processing instruction targets cannot contain colons");
        }
        catch (IllegalTargetException success) {}
        
        try {
          new ProcessingInstruction("", "test");
          fail("Processing instruction targets cannot be empty");
        }
        catch (IllegalTargetException success) {}
        
        try {
           new ProcessingInstruction(null, "test");
           fail("Processing instruction targets cannot be empty");
        }
        catch (IllegalTargetException success) {}
        
        try {
           new ProcessingInstruction("12345", "test");
           fail("Processing instruction targets must be NCNames");
        }
        catch (IllegalTargetException success) {}
        
        // test empty data allowed
        pi = new ProcessingInstruction("test", "");
        assertEquals("", pi.getValue());


    }

    public void testSetter() {

        try {
          pi.setValue("kjsahdj ?>");
          fail("Should raise an IllegalDataException");
        }
        catch (IllegalDataException success) {}
        try {
          pi.setValue("?>");
          fail("Should raise an IllegalDataException");
        }
        catch (IllegalDataException success) {}
        try {
          pi.setValue("kjsahdj ?> skhskjlhd");
          fail("Should raise an IllegalDataException");
        }
        catch (IllegalDataException success) {}
        try {
            pi.setValue(null);
            fail("Allowed null data");   
        }
        catch (IllegalDataException ex) {
            // success   
            assertNotNull(ex.getMessage());
        }
        
        // These should all work
        String[] testData = {"<html></html>",
          "name=value",
          "name='value'",
          "name=\"value\"",
          "salkdhsalkjhdkjsadhkj sadhsajkdh",
            "<?", "? >", " -- "
        };
        for (int i = 0; i < testData.length; i++) {
          pi.setValue(testData[i]);
          assertEquals(testData[i], pi.getValue());
        }

     }

    public void testNames() {
        assertEquals("test", pi.getTarget());
     }


    public void testEquals() {
        ProcessingInstruction pi1 
          = new ProcessingInstruction("test", "afaf");
        ProcessingInstruction pi2
          = new ProcessingInstruction("test", "afaf");
        ProcessingInstruction pi3 
          = new ProcessingInstruction("tegggst", "afaf");
        ProcessingInstruction pi4
          = new ProcessingInstruction("test", "1234");

        assertEquals(pi1, pi1);
        assertEquals(pi1.hashCode(), pi1.hashCode());
        assertTrue(!pi1.equals(pi2));
        assertTrue(!pi1.equals(pi3));
        assertTrue(!pi3.equals(pi4));
        assertTrue(!pi2.equals(pi4));
        assertTrue(!pi2.equals(pi3));
    }

    public void testCopy() {
        Element test = new Element("test");
        test.appendChild(pi);
        ProcessingInstruction c2 = (ProcessingInstruction) pi.copy();

        assertEquals(pi, c2);
        assertEquals(pi.getValue(), c2.getValue());
        assertTrue(!pi.equals(c2));
        assertNull(c2.getParent());
    }

    // Check passing in a string with correct surrogate pairs
    public void testCorrectSurrogates() {
        String goodString = "test: \uD8F5\uDF80  ";
        pi.setValue(goodString);
        assertEquals(goodString, pi.getValue());       
    }

    // Check passing in a string with broken surrogate pairs
    public void testSurrogates() {

        try {
            pi.setValue("test \uD8F5\uD8F5 test");
            fail("Allowed two high halves");
        }
        catch (IllegalDataException ex) {
            // success   
            assertNotNull(ex.getMessage());
        }
        
        try {
            pi.setValue("test \uDF80\uDF80 test");
            fail("Allowed two low halves");
        }
        catch (IllegalDataException ex) {
            // success   
            assertNotNull(ex.getMessage());
        }
        
        try {
            pi.setValue("test \uD8F5 \uDF80 test");
            fail("Allowed two halves split by space");
        }
        catch (IllegalDataException ex) {
            // success   
            assertNotNull(ex.getMessage());
        }

        try {
            pi.setValue("test \uDF80\uD8F5 test");
            fail("Allowed reversed pair");
        }
        catch (IllegalDataException ex) {
            // success   
            assertNotNull(ex.getMessage());
        }        
        
    }

    public void testLeafNode() {

        assertEquals(0, pi.getChildCount());
        assertTrue(!pi.hasChildren());
        try {
            pi.getChild(0);
            fail("Didn't throw IndexOutofBoundsException");
        }
        catch (IndexOutOfBoundsException ex) {
            // success   
        }
        
        assertNull(pi.getParent());

        Element element = new Element("test");
        element.appendChild(pi); 
        assertEquals(element, pi.getParent());
        assertEquals(pi, element.getChild(0));

        element.removeChild(pi);
        assertTrue(!element.hasChildren());

    }

}
