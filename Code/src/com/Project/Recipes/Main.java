package com.Project.Recipes;

import java.sql.*;
import java.util.*;
import static java.util.Map.entry;

// https://www.jetbrains.com/help/idea/creating-and-running-your-first-java-application.html#run_app
// FOR TESTING- What happens if we delete a recipe (this won't delete the ingredients from the ingredient table, right?
// TODO: Clean up main loop *
public class Main {
    public static void main(String[] args) throws ClassNotFoundException, SQLException{ // handle exceptions
        Connection c = DatabaseHelper.getConnection("RecipeBook");
        System.out.println("Successfully Connected to RecipeBook!");
        Scanner scanner = new Scanner(System.in);
        boolean programRuns = true;
        /*
        6. TODO: Give me a recipe! (have the program output a recipe)
         */
        // TODO: add a feature that allows the user to cancel their selection - ?
        while(programRuns){
            DatabaseHelper.printMenu();
            String userInput = scanner.nextLine();
            switch (userInput){
                // TODO: make more robust by checking user input *
                case "A":
                case "a":
                    System.out.println("Great! Let's add a recipe!");
                    String inputRecipeInfo = DatabaseHelper.getRecipeInfoFromUser(scanner);
                    DatabaseHelper.addRecipe(inputRecipeInfo, c);
                    break;
                case "I":
                case "i":
                    System.out.println("Please make sure the file name you are about to enter is in the same " +
                            "directory as this program.\nPlease enter the name of the csv file you wish to import " +
                            "(including the .csv at the end).");
                    userInput = scanner.nextLine();
                    DatabaseHelper.importCSV(userInput, c);
                    break;
                case "R":
                case "r":
                    System.out.println("Please enter the name of the recipe you would like to remove.");
                    userInput = scanner.nextLine();
                    boolean deleted = DatabaseHelper.removeElement(c, "RecipeInfo", "Title", userInput);
                    if(deleted){
                        System.out.println("Recipe deleted.");
                    }
                    else{
                        System.out.println("Recipe not found.");
                    }
                    break;
                case "C":
                case "c":
                    boolean inCalendar = true;
                    while(inCalendar){
                        System.out.println("We are in the calendar! What would you like to do?\n" +
                                "A - Add a recipe to a date\n" +
                                "V - View the calendar\n" +
                                "M - Main Menu");
                        userInput = scanner.nextLine();
                        switch(userInput){
                            case "A":
                            case "a":
                                boolean keepAsking = true;
                                while(keepAsking){
                                    System.out.println("Tell me the name of the recipe you made. (Press Enter to exit).");
                                    userInput = scanner.nextLine();
                                    if(userInput.equals("")){
                                        keepAsking = false;
                                    }
                                    else{
                                        try{
                                            ResultSet result = DatabaseHelper.findRows(c, "RecipeInfo", new String[]{"Title"},
                                                    new String[]{userInput}, new String[]{}, new String[]{});
                                            if(result.next()){
                                                keepAsking = false;
                                                Vector<String> rowResults = DatabaseHelper.getRowResults(result, new String[]{"RecipeID"});
                                                int recipeIdInt = Integer.parseInt(rowResults.get(0));
                                                System.out.println("Please enter the date you made this recipe in the form YYYY-MM-DD");
                                                userInput = scanner.nextLine();
                                                DatabaseHelper.addElement(c, "Calendar", new String[]{"LastEaten", "RecipeID"},
                                                        new Object[]{userInput, recipeIdInt});
                                            }
                                        }
                                        catch(SQLException e){
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }
                                break;
                            case "V":
                            case "v":
                                System.out.println("Let's look at the calendar! Please enter how many days back you " +
                                        "want to go from the most recent date.");
                                userInput = scanner.nextLine();
                                try {
                                    ResultSet result = DatabaseHelper.searchJoinedTables(c, Map.ofEntries(entry("Calendar",0),
                                            entry("RecipeInfo", 1)), new String[]{"Calendar.RecipeID", "RecipeInfo.RecipeId"},
                                            new String[]{}, new String[]{}, new String[]{"lastEaten"}, new String[]{"DESC"});
                                    if(result.next()){
                                        Vector<String> results= DatabaseHelper.getRowResults(result, new String[]{"lastEaten", "Title"});
                                        DatabaseHelper.printRows(results, Integer.parseInt(userInput));
                                    }
                                }
                                catch (SQLException e){
                                    System.out.println(e.getMessage());
                                }
                                break;
                            case "M":
                            case "m":
                                System.out.println("Exiting the Calendar.");
                                inCalendar = false;
                                break;
                            default:
                                System.out.println("Invalid Input. Please try again.");
                        }

                    }
                    break;
                case "S":
                case "s":
                    System.out.println("Let's find a recipe! Choose from the following options:\n" +
                            "N - Search by name\n" + // Query RecipeInfo
                            "I - Search by main ingredient\n" + // Query RecipeInfo
                            "T - Search by type\n" + // Query RecipeInfo
                            "C - Search by cuisine\n" +
                            "M - Main Menu"); // Query RecipeInfo
                            // TODO: add a search by serving size *
                    userInput = scanner.nextLine();
                    ResultSet results;
                    boolean keepAsking = true;
                    switch (userInput){

                        case "N":
                        case "n":
                            System.out.println("Awesome! We will search by name.");

                            while(keepAsking){
                                System.out.println("Enter the name of the recipe you're looking for. (Press Enter to quit.)");
                                userInput = scanner.nextLine();
                                if(!userInput.equals("")) {
                                    Integer recipeKey = DatabaseHelper.getKey(c, "RecipeID", "RecipeInfo",
                                            "Title", userInput);
                                    if (recipeKey != -1) {
                                        ArrayList<Recipe> recipes = DatabaseHelper.getAllResults(c, recipeKey.toString(), 'R');
                                        DatabaseHelper.printRecipes(recipes);
                                        DatabaseHelper.XMLOption(scanner, recipes);
                                    }
                                    else {
                                        System.out.println(
                                                String.format("Sorry! I couldn't find a recipe with the name %s.", userInput));
                                    }
                                }
                                else{
                                    System.out.println("Okay! Let's get out of here.");
                                    keepAsking = false;
                                }
                            }
                            break;
                        case "I":
                        case "i":
                            System.out.println("Awesome! We will search by main ingredient. (Press Enter to quit.)");
                            keepAsking = true;
                            while (keepAsking) {
                                System.out.println("Enter the main ingredient of the recipe you're looking for.");
                                userInput = scanner.nextLine();
                                if (!userInput.equals("")) {
                                    Integer ingredientKey = DatabaseHelper.getKey(c, "IngredientID", "Ingredients",
                                            "IngredientName", userInput);
                                    if (ingredientKey != -1) {
                                        ArrayList<Recipe> recipes = DatabaseHelper.getAllResults(c, ingredientKey.toString(), 'I');
                                        DatabaseHelper.printRecipes(recipes);
                                        DatabaseHelper.XMLOption(scanner, recipes);
                                    } else {
                                        System.out.println(
                                                String.format("Sorry! I couldn't find a recipe with %s as the main ingredient.", userInput));
                                    }
                                } else {
                                    System.out.println("Okay! Let's get out of here.");
                                    keepAsking = false;
                                }
                            }
                            break;
                        case "T":
                        case "t":
                            System.out.println("Awesome! We will search by type.");
                            keepAsking = true;
                            while(keepAsking){
                                System.out.println("Enter the type of the recipe you're looking for. (Press Enter to quit.)");
                                userInput = scanner.nextLine();
                                if(!userInput.equals("")){
                                    Integer typeKey = DatabaseHelper.getKey(c, "TypeID", "Types",
                                            "typeName", userInput);
                                    if(typeKey != -1){
                                        ArrayList<Recipe> recipes = DatabaseHelper.getAllResults(c, typeKey.toString(), 'T');
                                        DatabaseHelper.printRecipes(recipes);
                                        DatabaseHelper.XMLOption(scanner, recipes);
                                    }
                                    else{
                                        System.out.println(
                                                String.format("Sorry! I couldn't find a recipe with a type: %s.", userInput));
                                    }
                                }
                                else{
                                    System.out.println("Okay! Let's get out of here.");
                                    keepAsking = false;
                                }
                            }
                            break;
                        case "C":
                        case "c":
                            System.out.println("Awesome! We will search by cuisine. (Press Enter to quit.)");
                            keepAsking = true;
                            while(keepAsking){
                                System.out.println("Enter the cuisine of the recipe you're looking for.");
                                userInput = scanner.nextLine();
                                if(!userInput.equals("")) {
                                    Integer cuisineKey = DatabaseHelper.getKey(c, "CuisineID", "Cuisines",
                                            "cuisineName", userInput);
                                    if (cuisineKey != -1) {
                                        ArrayList<Recipe> recipes = DatabaseHelper.getAllResults(c, cuisineKey.toString(), 'C');
                                        DatabaseHelper.printRecipes(recipes);
                                        DatabaseHelper.XMLOption(scanner, recipes);
                                    } else {
                                        System.out.println(
                                                String.format("Sorry! I couldn't find a recipe with a cuisine %s.", userInput));
                                    }
                                }
                                else{
                                    System.out.println("Okay! Let's get out of here.");
                                    keepAsking = false;
                                }
                            }
                            break;
                        case "M":
                        case "m":
                            break;
                        default:
                            System.out.println("Invalid Input.");
                    }
                    break;
                case "Q":
                case "q":
                    System.out.println("Here are some quick recipes! They take 30 minutes or less to prepare.");
                    try{
                        results = DatabaseHelper.findRows(c, "QuickMeals", new String[]{},new String[]{},
                                new String[]{}, new String[]{});
                        if(results.next()) {
                            Vector<String> result = DatabaseHelper.getRowResults(results, new String[]{"Title"});
                            DatabaseHelper.printRows(result);
                        }
                    }
                    catch (SQLException e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case "F":
                case "f":
                    System.out.println("Here are some facts about your recipe book:");
                    DatabaseHelper.printFacts(c);
                    break;
                case "U":
                case "u":
                    keepAsking = true;
                    while(keepAsking) {
                        Integer keyInt = getIngredient(scanner,c);
                        if(keyInt == -1){
                            System.out.println("You don't have that ingredient in the database. Try a different one or " +
                                    "press enter to leave.");
                        }
                        else if(keyInt == -2){
                            System.out.println("Goodbye!");
                            keepAsking = false;
                        }
                        else{
                            boolean update = getInStockUpdate(scanner);
                            DatabaseHelper.updateStock(c, update, keyInt.toString());
                            System.out.println("Ingredient updated.");
                        }
                    }
                    break;
                case "X":
                case "x":
                    programRuns = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid Input. Please try again.");
            }
        }

    }

    //Returns -1 if the ingredient is not in the database and -2 if the user wants to escape.
    private static Integer getIngredient(Scanner in, Connection c){
        System.out.println("What ingredient do you want to update? (Press enter to escape.)");
        String userInput = in.nextLine();
        Integer output = -1;
        if(!userInput.equals("")){
            Integer keyInt = DatabaseHelper.getKey(c, "IngredientID", "Ingredients",
                    "IngredientName", userInput);
            if (keyInt != -1) {
                output = keyInt;
            }
        }
        else {
            return -2;
        }
        return output;
    }

    private static boolean getInStockUpdate(Scanner in) {
        boolean output = false;
        boolean keepAsking = true;
        while (keepAsking) {
            System.out.println("Is this ingredient in stock? (Y/N)");
            String userInput = in.nextLine();
            switch (userInput) {
                case "Y":
                case "y":
                    output = true;
                    keepAsking = false;
                    break;
                case "N":
                case "n":
                    output = false;
                    keepAsking = false;
                    break;
                default:
                    System.out.println("That is an invalid input. Please enter Y or N");
            }
        }
        return output;
    }
}
