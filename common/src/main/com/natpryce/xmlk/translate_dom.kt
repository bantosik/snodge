package com.natpryce.xmlk

import org.w3c.dom.CDATASection
import org.w3c.dom.Comment
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ProcessingInstruction
import org.w3c.dom.Text

fun Document.toXmlDocument(): XmlDocument =
    XmlDocument(null, childNodes.map {node -> node.toXmlNode()})

private fun Node.toXmlNode(): XmlNode =
    when(this) {
        is Element -> this.toXmlElement()
        is Text -> XmlText(wholeText, asCData = false)
        is CDATASection -> XmlText(wholeText, asCData = true)
        is ProcessingInstruction -> XmlProcessingInstruction(target, data.takeIf { it.isNotEmpty() })
        is Comment -> XmlComment(textContent ?: "")
        else -> throw IllegalArgumentException("cannot convert ${this::class.simpleName} to XmlNode")
    }

private fun Element.toXmlElement(): XmlElement {
    return XmlElement(
        name = QName(localName, namespaceURI, prefix),
        attributes = emptyMap(), // TODO parse attributes
        children = childNodes.map { it.toXmlNode() })
}

private fun NodeList.map(f: (Node)->XmlNode): List<XmlNode> =
    (0 until length).map { f(this.item(it)!!) }


fun XmlDocument.toDOM(implementation: DOMImplementation): Document {
    return implementation.createDocument("", "", null)
        .also { doc: Document ->
            children.forEach { child ->
                doc.appendChild(child.toDOM(doc))
            }
        }
}

private fun XmlNode.toDOM(doc: Document): Node =
    when (this) {
        is XmlText -> if (asCData) doc.createCDATASection(text) else doc.createTextNode(text)
        is XmlElement -> doc.createElementNS(name.namespaceURI, name.toDOM()).also { element ->
            attributes.forEach { (attrName, attrValue) ->
                element.setAttributeNS(attrName.namespaceURI, attrName.toDOM(), attrValue)
            }
            children.forEach { child ->
                element.appendChild(child.toDOM(doc))
            }
        }
        is XmlComment -> doc.createComment(text)
        is XmlProcessingInstruction -> {
            doc.createProcessingInstruction(target, data ?: "")
        }
    }

private fun QName.toDOM() = (prefix?.plus(":") ?: "") + localPart