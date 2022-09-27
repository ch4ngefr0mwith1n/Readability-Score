package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


public class Main {
    // ** formatiranje na dvije decimale: **
    private static final DecimalFormat df = new DecimalFormat("0.00");
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String inputText = "";

        try {
            inputText = readFromFile(args[0]);
        } catch (IOException e) {
            System.out.println("ERROR: Input file doesn't exist!");
        }

        List<String> wordsList = List.of(inputText.replaceAll("[,.:;!?]","").split("\\s"));
        int wordsCount = wordsList.size();

        List<String> sentencesList = List.of(inputText.split("[?!.]"));
        int sentencesCount = sentencesList.size();

        String allCharactersInText = inputText.replaceAll("\\s+", "");
        int charactersCount = allCharactersInText.length();

        int syllableCount = wordsList.stream()
                .map(Main::countSyllables)
                .reduce(0, Integer::sum);

        List<String> polysyllableWords = wordsList.stream()
                .filter(s -> countSyllables(s) > 2)
                .collect(Collectors.toList());
        int pollysyllableWordsCount = polysyllableWords.size();

        String scoreARI = df.format(getARIscore(charactersCount, wordsCount, sentencesCount));
        String scoreFK = df.format(getFKscore(wordsCount, sentencesCount, syllableCount));
        String scoreSMOG = df.format(getSMOGscore(pollysyllableWordsCount, sentencesCount));
        String scoreCL = df.format(getCLscore(charactersCount, wordsCount, sentencesCount));

        double averageScore = (getAge(Double.parseDouble(scoreARI)) +
                getAge(Double.parseDouble(scoreFK)) +
                getAge(Double.parseDouble(scoreSMOG)) +
                getAge(Double.parseDouble(scoreCL))) / 4.0;

        String scoreAVG = df.format(averageScore);

        System.out.printf("The text is:\n%s\n\n", inputText);
        printStats(wordsCount, sentencesCount, charactersCount, syllableCount, pollysyllableWordsCount);

        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all):");
        String testChoice = scanner.next();
        System.out.println();

        switch (testChoice.toUpperCase()) {
            case "ARI":
                System.out.printf("Automated Readability Index: %s (about %d-year-olds).\n",
                        scoreARI, getAge(Double.parseDouble(scoreARI)));
                break;
            case "FK":
                System.out.printf("Flesch–Kincaid readability tests: %s (about %d-year-olds).\n",
                        scoreFK, getAge(Double.parseDouble(scoreFK)));
                break;
            case "SMOG":
                System.out.printf("Simple Measure of Gobbledygook: %s (about %d-year-olds).\n",
                        scoreSMOG, getAge(Double.parseDouble(scoreSMOG)));
                break;
            case "CL":
                System.out.printf("Coleman–Liau index: %s (about %d-year-olds).\n",
                        scoreCL, getAge(Double.parseDouble(scoreCL)));
                break;
            case "ALL":
                System.out.printf("Automated Readability Index: %s (about %d-year-olds).\n",
                        scoreARI, getAge(Double.parseDouble(scoreARI)));
                System.out.printf("Flesch–Kincaid readability tests: %s (about %d-year-olds).\n",
                        scoreFK, getAge(Double.parseDouble(scoreFK)));
                System.out.printf("Simple Measure of Gobbledygook: %s (about %d-year-olds).\n",
                        scoreSMOG, getAge(Double.parseDouble(scoreSMOG)));
                System.out.printf("Coleman–Liau index: %s (about %d-year-olds).\n\n" +
                                "This text should be understood in average by %s-year-olds.",
                        scoreCL, getAge(Double.parseDouble(scoreCL)), scoreAVG);
                break;
            default:
                System.out.println("Unsupported type of score!");
        }
    }

    private static String readFromFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    // ** metoda za brojanje slogova unutar rijeci: **
    private static int countSyllables(String word) {
        // ** OBAVEZNO prebaciti u lowercase: **
        word = word.toLowerCase();
        // ** sva pojavljivanja za dva ili više samoglasnika: **
        int size = word.replaceAll("[aeiouy]{2,}", "a")
                // ** pojavljivanje samoglasnika "e" na kraju riječi: **
                .replaceAll("e$", "")
                // ** svi suglasnici unutar riječi: **
                .replaceAll("[^aeiouy]", "")
                .length();

        return Math.max(1, size);
    }

    private static double getARIscore(int characters, int words, int sentences) {
        return 4.71 * ((double) characters / (double) words) +
                0.5 * ((double) words / (double) sentences) - 21.43;
    }

    private static double getFKscore(int words, int sentences, int syllables) {
        return 0.39 * ((double) words / (double) sentences) + 11.8 * ((double) syllables / (double) words) - 15.59;
    }

    private static double getSMOGscore(int polysyllables, int sentences) {
        return 1.043 * Math.sqrt((double) polysyllables * (30/(double)sentences)) + 3.1291;
    }

    private static double getCLscore(int characters, int words, int sentences) {
        return 0.0588 * ((double) characters * 100 / (double) words) -
                0.296 * ((double) sentences * 100 / (double) words) - 15.8;
    }

    private static int getAge(double score) {
        final int[] age = {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 24, 25};
        int intScore = (int) Math.ceil(score);
        if (intScore >= age.length - 1) {
            intScore = age.length - 1;
        }
        return age[intScore];
    }

    private static void printStats(int words, int sentences, int characters, int syllables, int polysyllables) {
        System.out.printf("Words: %d\n" +
                "Sentences: %d\n" +
                "Characters: %d\n" +
                "Syllables: %d\n" +
                "Polysyllables: %d\n", words, sentences, characters, syllables, polysyllables);
    }
}
