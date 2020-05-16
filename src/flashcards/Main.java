package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    private static Map<String, String> flashcardsMap = new LinkedHashMap<>();   // map with flashcards - key: Name, value: definition
    private static Map<String, Integer> wrongAnswers = new LinkedHashMap<>();   // map where key is the same (Name) like in flashcardsMap,
                                                                                // but value have: counter for wrong answers
    private static ArrayList<String> inputAndOutputLog = new ArrayList<>();
    private static String output;                                               // String stores console output for Logging purpose
    private static String exportAs;                                             // String stores file name for export, passed as run argument
    private static boolean shouldExport = false;                                // if there was "-export" run argument shouldExport value will be true


    private static void menu() {
        boolean isRunning = true;

        while (isRunning) {                                                     // loop its Running, until user types exit
            output = "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):";
            System.out.println(output);
            inputAndOutputLog.add(output);

            String action = scanner.nextLine();                                 // String used to determine what user wants to do
            inputAndOutputLog.add(action);

            switch (action) {                                                   // after choosing action calling proper method
                case "add":
                    addCards();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "remove":
                    removeCard();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "import":
                    loadCardsFromFile();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "export":
                    saveAsFile();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "ask":
                    checkKnowledge();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "exit":                                                    // exiting case.
                    output = "Bye bye!";
                    System.out.println(output);
                    inputAndOutputLog.add(output);
                    isRunning = false;                                          // breaking while loop.
                    if (shouldExport) {                                         // if "-export" run argument is passed to the program
                        saveAsFile(exportAs);                                   // whole flashcardsMap will be saved as file,
                                                                                // with name passed as run argument (see importOrExport())
                    }
                    break;
                case "log":
                    log();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "hardest card":
                    hardestCard();
                    System.out.println();
                    inputAndOutputLog.add("\n");
                    break;
                case "reset stats":
                    resetStats();
                    System.out.println();
                    inputAndOutputLog.add("\"");
                    break;
                default:
                    output = "Sorry, wrong command! Try again.\n";
                    System.out.println(output);
                    inputAndOutputLog.add(output);
                    break;
            }
        }
    }

    private static void resetStats() {
        for (var entry : wrongAnswers.entrySet()) {
            wrongAnswers.replace(entry.getKey(), 0);        // wrongAnswers.put(entry.getKey(), 0);  <-- is replace better than put?
        }

        // wrongAnswers.replaceAll((k, v) -> 0);            // - nie znam tego jeszcze :(  easier, better way to set all values to 0

        output = "Card statistics has been reset.";
        System.out.println(output);
        inputAndOutputLog.add(output);
    }

    private static void hardestCard() {

        if (wrongAnswers.isEmpty()) {                                   // just a NPE error check. No need to check empty map :)
            output = "There are no cards with errors.";                 // Noting in map = no wrong answers.
            System.out.println(output);
            inputAndOutputLog.add(output);
            return;
        }


        ArrayList<Integer> numberOfErrorsPerCard = new ArrayList<>();   // this list stores every value (errors count) from wrongAnswers Map

        for (var entry : wrongAnswers.entrySet()) {                     // for every key(Name) we will take a matching number of wrong answers
            numberOfErrorsPerCard.add(entry.getValue());                // done for this card, and put it in array list
        }

        Collections.sort(numberOfErrorsPerCard);                        // here we will sort this List (from min to max),
                                                                        // to get number of most errors as last element.
                                                                        // That will be our hardest Card (or Cards)


        int errorCountOfHardestCard = numberOfErrorsPerCard.get(numberOfErrorsPerCard.size() - 1);
        // last element from sorted ArrayList, so the error count of Card with highest error rate

        int numberOfCardsWithSameErrorCount = 0;                        // we have to check if there is none, just one, or more Hardest Cards

        for (int content : numberOfErrorsPerCard) {                     // using forEach loop to set variable numberOfCardsWithSameErrorCount
            if (content == errorCountOfHardestCard) {                   // with matching number of cards with same error rate.
                numberOfCardsWithSameErrorCount++;
            }
        }

        String[] hardestCards = new String[numberOfCardsWithSameErrorCount];    // String array stores names of hardest cards.
                                                                                // Size is equal to number of cards with same highest error rate.
        for (int i = 0; i < numberOfCardsWithSameErrorCount; ) {                // numberOfCardsWithSameErrorCount == hardestCards.length;

            for (var entry : wrongAnswers.entrySet()) {                         // take every value (error count) from map,
                if (entry.getValue() == errorCountOfHardestCard) {              // check if it is equal to hardest card errors count
                    hardestCards[i] = entry.getKey();                           // if yes, then put cards name (key) in array
                    i++;                                                        // and increment i (index for array)
                }
            }
        }

        if (errorCountOfHardestCard == 0) {                             // no wrong answers
            output = "There are no cards with errors.";
        } else if (numberOfCardsWithSameErrorCount == 1) {              // just one hardest Card
            output = "The hardest card is \"" + hardestCards[0] + "\". You have " + errorCountOfHardestCard + " errors answering it.";
        } else {                                                        // two or more hardest cards
            output = "The hardest cards are ";
            for (int i = 0; i < hardestCards.length; i++) {             // loop to print all Strings from array
                output = output.concat("\"" + hardestCards[i] + "\"");
                if (i == hardestCards.length - 1) {                     // if it was the last element,
                    output = output.concat(". ");                       // then end String with dot
                } else {                                                // otherwise
                    output = output.concat(", ");                       // split elements with comma
                }
            }
            output = output.concat(" You have " + errorCountOfHardestCard + " errors answering them.");
        }

        System.out.println(output);
        inputAndOutputLog.add(output);
    }

    private static void addWrongAnswer(String wrongAnsweredCard) {
        int actualErrorCount = wrongAnswers.get(wrongAnsweredCard);
        actualErrorCount++;
        wrongAnswers.put(wrongAnsweredCard, actualErrorCount);
    }

    private static void log() {
        output = "File name:";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String saveLogAs = scanner.nextLine();
        inputAndOutputLog.add(saveLogAs);

        File logFile = new File(saveLogAs);
        try (FileWriter writer = new FileWriter(logFile)) {
            for (String loggedString : inputAndOutputLog) {
                writer.write(loggedString + "\n");
            }
        } catch (IOException e) {
            output = "An exception occurs " + e.getMessage();
            System.out.println(output);
            inputAndOutputLog.add(output);
        }

        output = "The log has been saved.";
        System.out.println(output);
        inputAndOutputLog.add(output);

    }

    private static void checkKnowledge() {
        // ================================================  TO DO ?  =================================================
        // create new String[] with all Keys from flashcardsMap
        // use Math.random() to pick random index from that String[].
        // Then use that name (key) to ask from original flashcardsMap
        // do it howManyTimesToAsk times
        //
        // leave check if it is answer from another card, and print (each time when wrong answered) the correct one
        // =============================================================================================================
        output = "How many times to ask?";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String input = scanner.nextLine();
        inputAndOutputLog.add(input);

        int howManyTimesToAsk = Integer.parseInt(input);
        int timesAsked = 0;
        boolean isRunning = true;


        while (isRunning) {

            for (var content : flashcardsMap.entrySet()) {                          // getting all keys and values from map
                output = "Print the definition of \"" + content.getKey() + "\":";   // asking for one Card
                System.out.println(output);
                inputAndOutputLog.add(output);
                timesAsked++;                                                       // after asking for answer, variable will increment

                String usersAnswer = scanner.nextLine();
                inputAndOutputLog.add(usersAnswer);

                if (usersAnswer.equals(content.getValue())) {       // if answer typed by user is the same like one from flashcard
                    output = "Correct answer.";                     // woo hoo, correct :D
                    System.out.println(output);
                    inputAndOutputLog.add(output);
                } else if (flashcardsMap.containsValue(usersAnswer)) {      // if answer provided by user is wrong,
                                                                            // but this answer has been found in map
                    for (Map.Entry<String, String> entry : flashcardsMap.entrySet()) {
                        if (entry.getValue().equalsIgnoreCase(usersAnswer)) {   // Look for which card answers the user,
                                                                                // and give correct answer, plus a hint
                            output = "Wrong answer. (The correct one is \"" + content.getValue() +
                                    "\", you've just written the definition of \"" + entry.getKey() + "\" card.)";
                            addWrongAnswer(content.getKey());                   // after wrong answer add error Count to map.
                            System.out.println(output);
                            inputAndOutputLog.add(output);
                            break;
                        }
                    }
                } else {                                        // wrong answer. Is also not as answer to any other card.
                    output = "Wrong answer. The correct one is \"" + content.getValue() + "\".";
                    addWrongAnswer(content.getKey());           // call method to add wrong answer count
                    System.out.println(output);
                    inputAndOutputLog.add(output);
                }

                if (howManyTimesToAsk == timesAsked) {          // if user is asked required number of times, quit loop
                    isRunning = false;
                    break;
                }
            }
        }
    }

    private static void saveAsFile() {
        output = "File name:";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String saveAs = scanner.nextLine();
        inputAndOutputLog.add(saveAs);

        File file = new File(saveAs);
        int numberOfPairs = 0;

        try (FileWriter writer = new FileWriter(file)) {

            // saving to file in format
            // name:definition:errors
            // nextName:nextDefinition:nextErrorCount

            for (var entry : flashcardsMap.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + ":" + wrongAnswers.get(entry.getKey()) + "\n");
                numberOfPairs++;
            }
        } catch (IOException e) {
            output = "An exception occurs " + e.getMessage();
            System.out.println(output);
            inputAndOutputLog.add(output);
        }
        output = numberOfPairs + " cards have been saved.";
        System.out.println(output);
        inputAndOutputLog.add(output);
    }

    private static void saveAsFile(String exportAs) {  // same as saveAsFile(), but uses run argument
        File file = new File(exportAs);
        int numberOfPairs = 0;

        try (FileWriter writer = new FileWriter(file)) {

            for (var entry : flashcardsMap.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + ":" + wrongAnswers.get(entry.getKey()) + "\n");
                numberOfPairs++;
            }
        } catch (IOException e) {
            output = "An exception occurs " + e.getMessage();
            System.out.println(output);
            inputAndOutputLog.add(output);
        }
        output = numberOfPairs + " cards have been saved.";
        System.out.println(output);
        inputAndOutputLog.add(output);
    }

    private static void loadCardsFromFile() {
        output = "File name:";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String fileName = scanner.nextLine();
        inputAndOutputLog.add(fileName);

        File file = new File(fileName);
        int numberOfLoadedCards = 0;

        try (Scanner sc = new Scanner(file).useDelimiter("[:\\n]")) {   // using delimiter and regex to add each String
                                                                        // between : and newLine to map
            while (sc.hasNext()) {
                String key = sc.next();
                String value = sc.next();
                String errors = sc.next();
                int errorsAsInt = Integer.parseInt(errors);
                flashcardsMap.put(key, value);
                wrongAnswers.put(key, errorsAsInt);
                numberOfLoadedCards++;
            }
            output = numberOfLoadedCards + " cards have been loaded.";
            System.out.println(output);
            inputAndOutputLog.add(output);
        } catch (FileNotFoundException e) {
            output = "File not found.";
            System.out.println(output);
            inputAndOutputLog.add(output);
        }
    }

    private static void loadCardsFromFile(String fileName) {    // initial importing, when starting program (run argument)
        File file = new File(fileName);

        int numberOfLoadedCards = 0;

        try (Scanner sc = new Scanner(file).useDelimiter("[:\\n]")) {
            while (sc.hasNext()) {
                String key = sc.next();
                String value = sc.next();
                String errors = sc.next();
                int errorsAsInt = Integer.parseInt(errors);
                flashcardsMap.put(key, value);
                wrongAnswers.put(key, errorsAsInt);
                numberOfLoadedCards++;
            }
            output = numberOfLoadedCards + " cards have been loaded.";
            System.out.println(output);
            inputAndOutputLog.add(output);
        } catch (FileNotFoundException e) {
            output = "File not found.";
            System.out.println(output);
            inputAndOutputLog.add(output);
        }
    }

    private static void removeCard() {
        output = "The card:";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String cardToRemove = scanner.nextLine();
        inputAndOutputLog.add(cardToRemove);

        if (flashcardsMap.containsKey(cardToRemove)) {
            flashcardsMap.remove(cardToRemove);
            wrongAnswers.remove(cardToRemove);              // No card? No errors.
            output = "The card has been removed.";
        } else {
            output = "Can't remove \"" + cardToRemove + "\": there is no such card.";
        }
        System.out.println(output);
        inputAndOutputLog.add(output);
    }

    private static void addCards() {
        output = "The card:";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String card = scanner.nextLine();
        inputAndOutputLog.add(card);

        if (flashcardsMap.containsKey(card)) {
            output = "The card " + card + " already exists.";
            System.out.println(output);
            inputAndOutputLog.add(output);
            return;
        }
        output = "The definition of the card:";
        System.out.println(output);
        inputAndOutputLog.add(output);

        String definition = scanner.nextLine();
        inputAndOutputLog.add(definition);

        if (flashcardsMap.containsValue(definition)) {
            output = "The definition " + definition + " already exists.";
            System.out.println(output);
            inputAndOutputLog.add(output);
            return;
        }

        flashcardsMap.put(card, definition);
        wrongAnswers.put(card, 0);                      // new card = no errors
        output = "The pair \"" + card + "\":\"" + definition + "\" has been added.";
        System.out.println(output);
        inputAndOutputLog.add(output);
    }

    private static void importOrExport(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-import".equals(args[i])) {
                loadCardsFromFile(args[i + 1]);
            }
            if ("-export".equals(args[i])) {
                shouldExport = true;
                exportAs = args[i + 1];
            }
        }
    }

    public static void main(String[] args) {

        importOrExport(args);
        menu();
        scanner.close();

    }
}
