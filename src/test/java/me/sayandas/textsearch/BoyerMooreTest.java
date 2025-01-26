package me.sayandas.textsearch;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

class BoyerMooreTest {

    @Test
    void Should_Return_Indices_When_Pattern_Matches() {
        String query = "test";
        BoyerMoore bm = new BoyerMoore(query);
        String text = "This test is a test";
        List<Integer> hits = bm.searchQuery(text, query);
        assertTrue(hits.contains(5));
        assertTrue(hits.contains(15));
    }


    @Test
    void Should_Find_All_Occurrences_Of_Repeated_Strings(){
        String query = "AAA";
        BoyerMoore bm = new BoyerMoore(query);
        String text = "AAAAA";
        List<Integer> hits = bm.searchQuery(text, query);

        String text2 = "AAABAAA";
        List<Integer> hits2 = bm.searchQuery(text2, query);


        assertEquals(hits.size(), 3);
        assertEquals(hits2.size(), 2);
    }
}