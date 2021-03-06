package com.natpryce.xmlk

import org.w3c.dom.DOMImplementation
import org.w3c.dom.parsing.DOMParser
import org.w3c.dom.parsing.XMLSerializer


fun String.toXmlDocument(): XmlDocument =
    DOMParser().parseFromString(this, "application/xml").toXmlDocument()

fun XmlDocument.toXmlString(implementation: DOMImplementation = defaultDOMImplementation()): String =
    XMLSerializer().serializeToString(toDOM(implementation))

