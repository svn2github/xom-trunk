/* Copyright 2002-2005 Elliotte Rusty Harold
   
   This library is free software; you can redistribute it and/or modify
   it under the terms of version 2.1 of the GNU Lesser General Public 
   License as published by the Free Software Foundation.
   
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
   GNU Lesser General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the 
   Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
   Boston, MA 02111-1307  USA
   
   You can contact Elliotte Rusty Harold by sending e-mail to
   elharo@metalab.unc.edu. Please include the word "XOM" in the
   subject line. The XOM home page is located at http://www.xom.nu/
*/

package nu.xom.canonical;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xml.sax.helpers.NamespaceSupport;

import nu.xom.Attribute;
import nu.xom.Comment;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Namespace;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParentNode;
import nu.xom.ProcessingInstruction;
import nu.xom.Serializer;
import nu.xom.Text;
import nu.xom.XPathContext;

/**
 * <p>
 *   Writes XML in the format specified by <a target="_top"
 *   href="http://www.w3.org/TR/2001/REC-xml-c14n-20010315">Canonical
 *   XML Version 1.0</a> or <a target="_top"
 *   href="http://www.w3.org/TR/2002/REC-xml-exc-c14n-20020718/">Exclusive
 *   XML Canonicalization Version 1.0</a>. 
 * </p>
 * 
 * @author Elliotte Rusty Harold
 * @version 1.1d5
 *
 */
public class Canonicalizer {

    private boolean withComments;
    private boolean exclusive = false;
    private CanonicalXMLSerializer serializer;
    
    private static Comparator comparator = new AttributeComparator();
    
    
    public final static String CANONICAL_XML =  
     "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    public final static String CANONICAL_XML_WITH_COMMENTS =  
     "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments";
    public final static String EXCLUSIVE_XML_CANONICALIZATION = 
      "http://www.w3.org/2001/10/xml-exc-c14n#";
    public final static String EXCLUSIVE_XML_CANONICALIZATION_WITH_COMMENTS = 
      "http://www.w3.org/2001/10/xml-exc-c14n#WithComments";
    
    
    private static class AttributeComparator implements Comparator {
        
        public int compare(Object o1, Object o2) {
            Attribute a1 = (Attribute) o1;   
            Attribute a2 = (Attribute) o2;   
            
            String namespace1 = a1.getNamespaceURI();
            String namespace2 = a2.getNamespaceURI();
            if (namespace1.equals(namespace2)) { 
                return a1.getLocalName().compareTo(a2.getLocalName());             
            }
            else if (namespace1.equals("")) {
                 return -1;   
            }
            else if (namespace2.equals("")) {
                 return 1;   
            }
            else { // compare namespace URIs
                return namespace1.compareTo(namespace2);               
            }
            
        }

    }
    
    
    /**
     * <p>
     *   Creates a <code>Canonicalizer</code> that outputs a 
     *   canonical XML document with comments.
     * </p>
     * 
     * @param out the output stream the document
     *     is written onto
     */
    public Canonicalizer(OutputStream out) {
        this(out, true, false);
    }

    
    /**
     * <p>
     *   Creates a <code>Canonicalizer</code> that outputs a 
     *   canonical XML document with or without comments.
     * </p>
     * 
     * @param out the output stream the document
     *     is written onto
     * @param withComments true if comments should be included 
     *     in the output, false otherwise
     */
    public Canonicalizer(
      OutputStream out, boolean withComments) {
        this(out, withComments, false);
    }


    /**
     * <p>
     * Creates a <code>Canonicalizer</code> that outputs a 
     * canonical XML document with or without comments,
     * using either the original or the exclusive canonicalization
     * algorithm. 
     * </p>
     * 
     * @param out the output stream the document
     *     is written onto
     * @param withComments true if comments should be included 
     *     in the output, false otherwise
     * @param exclusive true if exclusive XML canonicalization 
     *     should be performed, false if regular XML canonicalization
     *     should be performed
     */
    public Canonicalizer(
      OutputStream out, boolean withComments, boolean exclusive) {
        
        this.serializer = new CanonicalXMLSerializer(out);
        serializer.setLineSeparator("\n");
        this.withComments = withComments;
        this.exclusive = exclusive;
        
    }


    /**
     * <p>
     * Creates a <code>Canonicalizer</code> that outputs a 
     * canonical XML document using the specified algorithm.Currently, four 
     * algorithms are defined and supported:
     * </p>
     * 
     * <ul>
     * <li>Canonical XML without comments: 
     * <code>http://www.w3.org/TR/2001/REC-xml-c14n-20010315</code></li>
     * <li>Canonical XML with comments: 
     * <code>http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments</code></li>
     * <li>Exclusive XML canonicalization without comments: 
     * <code>http://www.w3.org/2001/10/xml-exc-c14n#</code></li>
     * <li>Exclusive XML canonicalization with comments: 
     * <code>http://www.w3.org/2001/10/xml-exc-c14n#WithComments</code></li>
     * </ul>
     * 
     * @param out the output stream the document
     *     is written onto
     * @param algorithm the URI for the canonicalization algorithm
     * 
     * @throws IllegalArgumentException if the algorithm is 
     *     not recognized
     * 
     */
    public Canonicalizer(
      OutputStream out, String algorithm) {
        
        if (algorithm == null) {
            throw new NullPointerException("Null algorithm");
        }
        this.serializer = new CanonicalXMLSerializer(out);
        serializer.setLineSeparator("\n");
        if (algorithm.equals(CANONICAL_XML)) {
            this.withComments = false;
            this.exclusive = false;
        }
        else if (algorithm.equals(CANONICAL_XML_WITH_COMMENTS)) {
            this.withComments = true;
            this.exclusive = false;
        }
        else if (algorithm.equals(EXCLUSIVE_XML_CANONICALIZATION)) {
            this.withComments = false;
            this.exclusive = true;            
        }
        else if (algorithm.equals(EXCLUSIVE_XML_CANONICALIZATION_WITH_COMMENTS)) {
            this.withComments = true;
            this.exclusive = true;            
        }
        else {
            // custom exception????
            throw new IllegalArgumentException(
              "Unsupported canonicalization algorithm: " + algorithm);
        }
        
    }


    private class CanonicalXMLSerializer extends Serializer {
        
        // If nodes is null we're canonicalizing all nodes;
        // the entire document; this is somewhat easier than when
        // canonicalizing only a document subset embedded in nodes
        private Nodes nodes;
        private NamespaceSupport inScope;

        /**
         * <p>
         *   Creates a <code>Serializer</code> that outputs a 
         *   canonical XML document with or without comments.
         * </p>
         * 
         * @param out the <code>OutputStream</code> the document
         *     is written onto
         * @param withComments true if comments should be included 
         *     in the output, false otherwise
         */
        CanonicalXMLSerializer(OutputStream out) {
            super(out);
            setLineSeparator("\n");
        }

        
        /**
         * <p>
         * Serializes a document onto the output 
         * stream using the canonical XML algorithm.
         * </p>
         * 
         * @param doc the <code>Document</code> to serialize
         * 
         * @throws IOException if the underlying <code>OutputStream</code>
         *      encounters an I/O error
         */
         public final void write(Document doc) throws IOException {
            
            inScope = new NamespaceSupport();
            int position = 0;        
            while (true) {
                Node child = doc.getChild(position);
                if (nodes == null || child instanceof Element || nodes.contains(child)) {
                    writeChild(child); 
                    if (child instanceof ProcessingInstruction) breakLine();
                    else if (child instanceof Comment && withComments) {
                        breakLine();
                    }
                }
                position++;
                if (child instanceof Element) break;
            }       
            
            for (int i = position; i < doc.getChildCount(); i++) {
                Node child = doc.getChild(i);
                if (nodes == null || child instanceof Element || nodes.contains(child)) {
                    if (child instanceof ProcessingInstruction) breakLine();
                    else if (child instanceof Comment && withComments) {
                        breakLine();
                    }
                    writeChild(child);
                }
            }
            
            flush();
            
        }  
     
         
        /**
         * <p>
         * Serializes an element onto the output stream using the canonical
         * XML algorithm.  The result is guaranteed to be well-formed. 
         * If <code>element</code> does not have a parent element, it will
         * also be namespace well-formed.
         * </p>
         * 
         * @param element the <code>Element</code> to serialize
         * 
         * @throws IOException if the underlying <code>OutputStream</code>
         *     encounters an I/O error
         */
        protected final void write(Element element) 
          throws IOException {

            // treat empty elements differently to avoid an
            // instanceof test
            if (element.getChildCount() == 0) {
                writeStartTag(element, false);
                writeEndTag(element);                
            }
            else {
                Node current = element;
                boolean end = false;
                int index = -1;
                int[] indexes = new int[10];
                int top = 0;
                indexes[0] = -1;
                while (true) {                   
                    if (!end && current.getChildCount() > 0) {
                       writeStartTag((Element) current, false);
                       current = current.getChild(0);
                       index = 0;
                       top++;
                       indexes = grow(indexes, top);
                       indexes[top] = 0;
                    }
                    else {
                        if (end) {
                            writeEndTag((Element) current);
                            if (current == element) break;
                        }
                        else {
                            writeChild(current);
                        }
                        end = false;
                        ParentNode parent = current.getParent();
                        if (parent.getChildCount() - 1 == index) {
                            current = parent;
                            top--;
                            if (current != element) {
                                parent = current.getParent();
                                index = indexes[top];
                            }
                            end = true;
                        }
                        else {
                            index++;
                            indexes[top] = index;
                            current = parent.getChild(index);
                        }
                    }
                }   
            }
            
        } 
    
        
        private int[] grow(int[] indexes, int top) {
            
            if (top < indexes.length) return indexes;
            int[] result = new int[indexes.length*2];
            System.arraycopy(indexes, 0, result, 0, indexes.length);
            return result;
            
        }


        protected void writeStartTag(Element element, boolean isEmpty) 
          throws IOException {
            
            boolean writeElement = nodes == null || nodes.contains(element);
            if (writeElement) {
                inScope.pushContext();
                writeRaw("<");
                writeRaw(element.getQualifiedName());
            }
            
            SortedMap map = new TreeMap();
            if (nodes == null) {
                ParentNode parent = element.getParent();
                Element parentElement = null;
                if (parent instanceof Element) {
                    parentElement = (Element) parent; 
                } 
                for (int i = 0; 
                     i < element.getNamespaceDeclarationCount(); 
                     i++) {
                    String prefix = element.getNamespacePrefix(i);
                    String uri = element.getNamespaceURI(prefix);
                    
                    if (uri.equals(inScope.getURI(prefix))) {
                        continue;
                    }
                    else if (exclusive) {
                        if (visiblyUtilized(element, prefix, uri)) {
                            map.put(prefix, uri);
                        }
                    }
                    else if (uri.equals("")) {
                        // no need to say xmlns=""
                        if (parentElement == null) continue;    
                        if ("".equals(parentElement.getNamespaceURI())) {
                            continue;
                        }
                        map.put(prefix, uri);
                    }
                    else {
                        map.put(prefix, uri);
                    }
                    
                } 
                
                writeNamespaceDeclarations(map);
                
            }
            else {
                int position = indexOf(element);
                // do we need to undeclare a default namespace?
                // You know, should I instead create an output tree and then just
                // canonicalize that? probably not
                if (position != -1 && "".equals(element.getNamespaceURI())) {
                    ParentNode parent = element.getParent();
                    // Here we have to check for the nearest default on parents in the
                    // output tree, not the input tree
                    while (parent != null && parent instanceof Element 
                      && !(nodes.contains(parent))) {
                        parent = parent.getParent();
                    }
                    if (parent != null && parent instanceof Element) {
                        String uri = ((Element) parent).getNamespaceURI("");
                        if (! "".equals(uri)) {
                            map.put("", "");
                        }
                    }
                }
                
                for (int i = position+1; i < nodes.size(); i++) {
                    Node next = nodes.get(i);
                    if ( !(next instanceof Namespace) ) break;
                    Namespace namespace = (Namespace) next;
                    String prefix = namespace.getPrefix();
                    String uri = namespace.getValue();
                    
                    if (uri.equals(inScope.getURI(prefix))) {
                        continue;
                    }
                    else if (exclusive) {
                        if (visiblyUtilized(element, prefix, uri)) {
                            map.put(prefix, uri);
                        }
                    }
                    else {
                        map.put(prefix, uri);
                    }
                    
                } 
                
                writeNamespaceDeclarations(map);
                
            }
            
            Attribute[] sorted = sortAttributes(element);        
            for (int i = 0; i < sorted.length; i++) {
                if (nodes == null || nodes.contains(sorted[i]) 
                   || (sorted[i].getNamespaceURI().equals(Namespace.XML_NAMESPACE) 
                       && sorted[i].getParent() != element)) {
                    writeRaw(" ");
                    write(sorted[i]);
                }
            }       
            
            if (writeElement) {
                writeRaw(">");
            }
            
        }


        private void writeNamespaceDeclarations(SortedMap map) throws IOException {

            Iterator prefixes = map.keySet().iterator();
            while (prefixes.hasNext()) {
                String prefix = (String) prefixes.next();
                String uri = (String) map.get(prefix);
                writeRaw(" ");
                writeNamespaceDeclaration(prefix, uri);
                inScope.declarePrefix(prefix, uri);
            }
            
        }


        private boolean visiblyUtilized(Element parent, String prefix, String uri) {

            for (int i = 0; i < parent.getNamespaceDeclarationCount(); i++) {
                String pfx = parent.getNamespacePrefix(i);
                if (prefix.equals(pfx)) {
                    return noOutputAncestorUsesPrefix(parent, prefix, uri);
                }
            }
            
            return false;
            
        }


        private boolean noOutputAncestorUsesPrefix(Element original, String prefix, String uri) {

            ParentNode parent = original.getParent();
            while (parent != null && !(parent instanceof Document)) {
                if (nodes.contains(parent)) {
                    Element element = (Element) parent;
                    for (int i = 0; i < element.getNamespaceDeclarationCount(); i++) {
                        String current = element.getNamespacePrefix(i);
                        if (current.equals(prefix)) {
                            String newURI = element.getNamespaceURI(prefix);
                            return ! newURI.equals(uri);
                        }
                    }
                }
                parent = parent.getParent();
            }
            return true;
            
        }


        // ???? move into Nodes?
        private int indexOf(Element element) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) == element) return i;
            }
            return -1;
        }


        protected void write(Attribute attribute) throws IOException {
            
            writeRaw(attribute.getQualifiedName());
            writeRaw("=\"");
            writeRaw(prepareAttributeValue(attribute));
            writeRaw("\"");
            
        }
        
        
        protected void writeEndTag(Element element) throws IOException {
            
            if (nodes == null || nodes.contains(element)) {
                writeRaw("</");
                writeRaw(element.getQualifiedName());
                writeRaw(">");
                inScope.popContext();
            }
            
        }    
        
        private final XPathContext xmlcontext = new XPathContext("xml", Namespace.XML_NAMESPACE);
        
        private Attribute[] sortAttributes(Element element) {
    
            Map nearest = new TreeMap();
            // add in any inherited xml: attributes 
            if (!exclusive && nodes != null && indexOf(element) != -1) {
                // grab all xml: attributes
                Nodes attributes = element.query("ancestor::*/@xml:*", xmlcontext);
                if (attributes.size() != 0) {
                    for (int i = attributes.size()-1; i >= 0; i--) {
                        Attribute a = (Attribute) attributes.get(i);
                        String name = a.getLocalName();
                        if (element.getAttribute(name, Namespace.XML_NAMESPACE) != null) {
                            // this element already has that attribute
                            continue;
                        }
                        if (! nearest.containsKey(name)) {
                            Element parent = (Element) a.getParent();
                            if (indexOf(parent) == -1) {
                                nearest.put(name, a);
                            }
                            else {
                                nearest.put(name, null);
                            }
                        }
                    }
                }
                
                // remove null values
                Iterator iterator = nearest.values().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next() == null) iterator.remove();
                }
                
            }
            
            int localCount = element.getAttributeCount();
            Attribute[] result 
              = new Attribute[localCount + nearest.size()];
            for (int i = 0; i < localCount; i++) {
                result[i] = element.getAttribute(i); 
            }
            
            Iterator iterator = nearest.values().iterator();
            for (int j = localCount; j < result.length; j++) {
                result[j] = (Attribute) iterator.next();
            }
            
            Arrays.sort(result, comparator);       
            
            return result;        
            
        }
    
        
        private String prepareAttributeValue(Attribute attribute) {
    
            String value = attribute.getValue();
            StringBuffer result = new StringBuffer(value.length());
    
            if (attribute.getType().equals(Attribute.Type.CDATA)
              || attribute.getType().equals(Attribute.Type.UNDECLARED)) {
                char[] data = value.toCharArray();
                for (int i = 0; i < data.length; i++) {
                    char c = data[i];
                    if (c == '\t') {
                        result.append("&#x9;");
                    }
                    else if (c == '\n') {
                        result.append("&#xA;");
                    }
                    else if (c == '\r') {
                        result.append("&#xD;");
                    }
                    else if (c == '\"') {
                        result.append("&quot;");
                    }
                    else if (c == '&') {
                        result.append("&amp;");
                    }
                    else if (c == '<') {
                        result.append("&lt;");
                    }
                    else { 
                        result.append(c);   
                    }
                }
            }
            else {
                // According to the spec, "Whitespace character references
                // other than &#x20; are not affected by attribute value 
                // normalization. For parsed documents, the parser will  
                // still replace these with the actual character. I am 
                // going to assume that if one is found here, that the 
                // user meant to put it there; and so we will escape it 
                // with a character reference
                char[] data = value.toCharArray();
                boolean seenFirstNonSpace = false;
                for (int i = 0; i < data.length; i++) {
                    if (data[i] == ' ') {
                        if (i != data.length-1 && data[i+1] != ' ' && seenFirstNonSpace) {
                             result.append(data[i]); 
                        }
                        continue;
                    } 
                    seenFirstNonSpace = true;
                    if (data[i] == '\t') {
                        result.append("&#x9;");
                    }
                    else if (data[i] == '\n') {
                        result.append("&#xA;");
                    }
                    else if (data[i] == '\r') {
                        result.append("&#xD;");
                    }
                    else if (data[i] == '\"') {
                        result.append("&quot;");
                    }
                    else if (data[i] == '&') {
                        result.append("&amp;");
                    }
                    else if (data[i] == '<') {
                        result.append("&lt;");
                    }
                    else {
                        result.append(data[i]);
                    }
                }
            }
    
            return result.toString();    
            
        }
        
        
        /**
         * <p>
         * Serializes a <code>Text</code> object
         * onto the output stream using the UTF-8 encoding.
         * The reserved characters &lt;, &gt;, and &amp;
         * are escaped using the standard entity references such as
         * <code>&amp;lt;</code>, <code>&amp;gt;</code>, 
         * and <code>&amp;amp;</code>.
         * </p>
         * 
         * @param text the <code>Text</code> to serialize
         * 
         * @throws IOException  if the underlying <code>OutputStream</code>
         *     encounters an I/O error
         */
        protected final void write(Text text) throws IOException {
            
            if (nodes == null || nodes.contains(text)) {
                String input = text.getValue();
                StringBuffer result = new StringBuffer(input.length());
                for (int i = 0; i < input.length(); i++) {
                    char c = input.charAt(i);
                    if (c == '\r') {
                        result.append("&#xD;");
                    }
                    else if (c == '&') {
                        result.append("&amp;");
                    }
                    else if (c == '<') {
                        result.append("&lt;");
                    }
                    else if (c == '>') {
                        result.append("&gt;");
                    }
                    else { 
                        result.append(c);   
                    }            
                }
                writeRaw(result.toString());
            }
            
        }   
    
        
        /**
         * <p>
         * Serializes a <code>Comment</code> object
         * onto the output stream if and only if this
         * serializer is configured to produce canonical XML
         * with comments.
         * </p>
         * 
         * @param comment the <code>Comment</code> to serialize
         * 
         * @throws IOException if the underlying <code>OutputStream</code>
         *     encounters an I/O error
         */
        protected final void write(Comment comment) 
          throws IOException {
            if (withComments && (nodes == null || nodes.contains(comment))) {
                super.write(comment);
            }
        }
        
        
        protected final void write(ProcessingInstruction pi) 
          throws IOException {
            if (nodes == null || nodes.contains(pi)) {
                super.write(pi);
            }
        }
        
        
        /**
         * <p>
         * Does nothing because canonical XML does not include
         * document type declarations.
         * </p>
         * 
         * @param doctype the document type declaration to serialize
         */
        protected final void write(DocType doctype) {
            // DocType is not serialized in canonical XML
        } 
       
        
    }

    
    /**
     * <p>
     * Serializes a document onto the output 
     * stream using the specified canonicalization algorithm.
     * </p>
     * 
     * @param doc the document to serialize
     * 
     * @throws IOException if the underlying <code>OutputStream</code>
     *      encounters an I/O error
     */
    public final void write(Document doc) throws IOException {  
        serializer.nodes = null;
        serializer.write(doc);        
        serializer.flush();
    }  
 
    
    /**
     * <p>
     * Serializes a document subset selected by an XPath expression
     * onto the output stream using the canonical XML algorithm.
     * Only nodes selected by the XPath expression are output.
     * Children are not output unless they are specifically selected.
     * Selecting an element does not automatically select all the  
     * element's children and attributes. Not selecting an element
     * does not imply that its children and attributes will not be
     * output. 
     * </p>
     * 
     * @param doc the document to serialize
     * @param xpath the XPath expression that identifies the nodes to
     *     canonicalize
     * @param context the namespace bindings used when resolving the 
     *     XPath expression
     * 
     * @throws IOException if the underlying <code>OutputStream</code>
     *     encounters an I/O error
     * @throws XPathException if the XPath expression is syntactically
     *     incorrect
     */
    public final void write(Document doc, String xpath, XPathContext context) 
      throws IOException {  
        
        Nodes selected = doc.query(xpath, context);
        serializer.nodes = selected;
        serializer.write(doc);        
        serializer.flush();
        
    }  
 
    
}