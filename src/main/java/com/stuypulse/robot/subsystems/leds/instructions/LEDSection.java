package com.stuypulse.robot.subsystems.leds.instructions;

import com.stuypulse.robot.constants.Settings;
import com.stuypulse.robot.util.SLColor;

import edu.wpi.first.wpilibj.AddressableLEDBuffer;

public class LEDSection implements LEDInstruction {
    public SLColor[] sections;
    public int[] lengths;

    public LEDSection(SLColor[] sections, int[] lengths) {
        if (sections.length != lengths.length - 1) {
            throw new IllegalArgumentException("Mismatching section and length lengths");
        }
        this.sections = sections;
        this.lengths = lengths;
    }

    public LEDSection(SLColor[] sections){
        this.sections = sections;

        int totalLEDLength = Settings.LED.LED_LENGTH;
        int sectionLength = totalLEDLength / sections.length;
        int extraLEDS = totalLEDLength % sections.length;

        this.lengths = new int[sections.length];
        //extra LEDs get distributed to the first few sections
        for (int i = 0; i < sections.length; i++){
            int offset = Math.min(extraLEDS, i);
            int extraLEDForCurrentSection = (i < extraLEDS ? 1 : 0);
            this.lengths[i] = (i + 1) * sectionLength + offset + extraLEDForCurrentSection;
        }
    }

    @Override
    public void setLED(AddressableLEDBuffer ledBuffer){
        int sectionIndex = 0;
        
        for (int ledIndex = 0; ledIndex < ledBuffer.getLength(); ledIndex++){
            if (lengths[sectionIndex] < ledIndex){
                sectionIndex++;
            }

            SLColor color = sections[sectionIndex];
            ledBuffer.setRGB(ledIndex,
                color.getRed(),
                color.getGreen(),
                color.getBlue()
            );
        }
    }
        
    // @Override
    // public void setLED(AddressableLEDBuffer ledsBuffer) {
    //     int sectionLength = ledsBuffer.getLength() / sections.length;
        
    //     int offset = 0;

    //     for (int i = 0; i < sections.length; i++) {
    //         for (int j = 0; j < sectionLength + i % 2; j++) {
    //             ledsBuffer.setRGB(i * sectionLength + offset + j, sections[i].getRed(), sections[i].getGreen(), sections[i].getBlue());
    //         }
    //         if (i % 2 == 1) offset += 1;
    //     }  
    // }
}
