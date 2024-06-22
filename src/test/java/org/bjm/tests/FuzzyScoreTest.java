/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package org.bjm.tests;

import java.util.Locale;
import org.apache.commons.text.similarity.FuzzyScore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author singh
 */
public class FuzzyScoreTest {
    
    public FuzzyScoreTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /*@Test
    public void testFuzzyStrings() {
        String str1="Kuldeep Singh";
        String str2="Kuldep Singh";
        String str3="Kuldip Singh";
        FuzzyScore fuzzyScore=new FuzzyScore(Locale.ENGLISH);
        int score1=fuzzyScore.fuzzyScore(str1, str2);
        System.out.println("fuzzyScore.fuzzyScore(str1, str2) is: "+score1);
        int score2=fuzzyScore.fuzzyScore(str1, str3);
        System.out.println("fuzzyScore.fuzzyScore(str1, str3) is: "+score2);
        assertTrue(score1>10);
        assertTrue(score2>10);
    }*/
    
    @Test
    public void testFuzzyStrings2() {
        String str1="Kirat";
        String str2="Kerat";
        FuzzyScore fuzzyScore=new FuzzyScore(Locale.ENGLISH);
        int score1=fuzzyScore.fuzzyScore(str1, str2);
        System.out.println("fuzzyScore.fuzzyScore(str1, str2) is: "+score1);
        assertTrue(score1<10);
    }
}
