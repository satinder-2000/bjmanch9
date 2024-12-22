/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.bjm.tests;

import java.time.LocalDateTime;

/**
 *
 * @author singh
 */
public class LocalDateTimeTest {
    
    public static void main(String[] args){
        LocalDateTime t=LocalDateTime.now();
        System.out.println(t.getDayOfMonth()+"-"+t.getMonthValue()+"-"+t.getYear());
    }
    
}
