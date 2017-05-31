package com.natpryce.snodge.json

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.snodge.combine
import com.natpryce.snodge.mutants
import com.natpryce.snodge.plus
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.charset.Charset
import java.util.Random

class JsonMutagenTest {
    val random = Random()
    
    private val mutagen = addObjectProperty(JsonNull.INSTANCE) + addArrayElement(JsonNull.INSTANCE)
    
    @Test
    fun `can add null object property`() {
        val doc = obj(
            "alice" to 1,
            "bob" to 2)
        
        val mutations = random.mutants(mutagen, 1, doc)
        
        assertThat("should only be one mutation", mutations.size, equalTo(1))
        
        assertThat(mutations.first(), equalTo(obj(
            "alice" to 1,
            "bob" to 2,
            "x" to null) as JsonElement))
    }
    
    @Test
    fun `can add null array property`() {
        val doc = list(1, 2, 3)
        
        val mutations = random.mutants(mutagen, 1, doc)
        
        assertThat("should be one mutation",
            mutations, equalTo<List<JsonElement>>(listOf(list(1, 2, 3, null))))
    }
    
    @Test
    fun `can return multiple mutations`() {
        val doc = obj(
            "num" to 1,
            "list" to list(1, 2, 3))
        
        val mutatedDocs = random.mutants(mutagen, 2, doc)
        
        assertThat("number of mutations", mutatedDocs.size, equalTo(2))
        
        assertTrue(mutatedDocs.contains(obj(
            "num" to 1,
            "list" to list(1, 2, 3),
            "x" to null)))
        
        assertTrue(mutatedDocs.contains(obj(
            "num" to 1,
            "list" to list(1, 2, 3, null))))
    }
    
    @Test
    fun `returns a random sample of all possible mutations`() {
        val doc = list(list(1, 2), list(list(3, 4), list(5, 6, list(7, 8)), list(9, 10)), list(11, 12))
        
        random.setSeed(0)
        val mutatedDocs = random.mutants(mutagen, 2, doc)
        
        assertThat("number of mutations", mutatedDocs.size, equalTo(2))
        
        random.setSeed(99)
        assertThat(random.mutants(mutagen, 2, doc), !equalTo(random.mutants(mutagen, 2, doc)))
    }
    
    @Test
    fun `will not return more mutations than can be generated by the mutagens`() {
        val doc = list(list(1, 2), list(3, 4))
        
        assertThat("number of mutations", random.mutants(mutagen, 10, doc).size, equalTo(3))
    }
    
    @Test
    fun `can mutate json text`() {
        val gson = Gson()
        val original = """
        {
            "num": 1,
            "list": [1,2,3]
        }
        """
        
        val mutated = random.mutants(mutagen.forStrings(), 1, original).first()
        
        assertThat(mutated, !equalTo(original))
        
        gson.canParse(mutated)
    }
    
    @Test
    fun `can mutate encoded json text`() {
        val charset = Charset.forName("UTF-8")
        
        val gson = Gson()
        val originalString = gson.toJson(obj(
            "num" to 1,
            "list" to list(1, 2, 3)))
        
        val originalBytes = originalString.toByteArray(charset)
        
        val mutatedBytes = random.mutants(mutagen.forEncodedStrings(charset), 2, originalBytes).first()
        
        assertThat(mutatedBytes, !equalTo(originalBytes))
        
        val mutatedString = String(mutatedBytes, charset)
        
        gson.canParse(mutatedString)
    }
    
    private fun Gson.canParse(mutated: String) {
        // Does not blow up
        fromJson(mutated, JsonElement::class.java)
    }
}
