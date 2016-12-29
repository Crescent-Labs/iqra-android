package com.mmmoussa.iqra.objects;

public class Ayah {
    private String arabicAyah;
    private String surahName;
    private String translatedAyah;
    private String translatedSurahName;

    private int ayahNum;
    private int surahNum;

    public Ayah(String arabicAyah, String surahName, String translatedAyah, String translatedSurahName, int ayahNum, int surahNum) {
        this.arabicAyah = arabicAyah;
        this.surahName = surahName;
        this.translatedAyah = translatedAyah;
        this.translatedSurahName = translatedSurahName;
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

    public String getTranslatedSurahName() {
        return translatedSurahName;
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
