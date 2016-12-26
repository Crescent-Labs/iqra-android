package com.crescentlabs.iqra.objects;

public class Ayah {
    private String arabicAyah;
    private String surahName;
    private String translatedAyah;
    private String translatedSurah;

    private int ayahNum;
    private int surahNum;

    public Ayah(String arabicAyah, String surahName, String translatedAyah, String translatedSurah, int ayahNum, int surahNum) {
        this.arabicAyah = arabicAyah;
        this.surahName = surahName;
        this.translatedAyah = translatedAyah;
        this.translatedSurah = translatedSurah;
        this.ayahNum = ayahNum;
        this.surahNum = surahNum;
    }

    public String getArabicAyah() {
        return arabicAyah;
    }

    public String getSurahName() {
        return surahName;
    }

    public String getTranslatedAyah() {
        return translatedAyah;
    }

    public String getTranslatedSurah() {
        return translatedSurah;
    }

    public int getAyahNum() {
        return ayahNum;
    }

    public int getSurahNum() {
        return surahNum;
    }

    @Override
    public String toString() {
        return "surahNum:" + surahNum + ", ayahNum:" + ayahNum;
    }
}
