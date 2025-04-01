import java.util.*;

public class SolutionWithBacktracking {
    public static void main(String[] args) {
        int POPULATION_SIZE = 1000;
        int ROUNDS_WITHOUT_CHANGE = 10;
        double CROSSOVER_RATE = 0.35;
        double MUTATION_RATE = 0.15;
        final Random random = new Random();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite as cidades solicitadas separadas por espaço (ex.: 6 0 3):");
        String input = scanner.nextLine();
        int[] citiesRequested = parseCities(input);

        int[][] cities = FileService.readInputFile("cidades.txt");
        if (cities == null) {
            System.out.println("Erro ao abrir arquivo");
            return;
        }

        // start population
        ArrayList<boolean[][]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            boolean[][] person = new boolean[cities.length][cities.length];
            for (int j = 0; j < cities.length; j++) {
                for (int k = 0; k < cities.length; k++) {
                    person[j][k] = random.nextDouble() > 0.8;
                }
            }
            population.add(person);
        }

        int rounds = 0;
        IsValidFeedback best = null;
        while (rounds < ROUNDS_WITHOUT_CHANGE) {
            System.out.println("----- NEW ROUND -----");
            // filtrar válidos
            ArrayList<IsValidFeedback> validPopulation = new ArrayList<>();
            IsValidFeedback bestOfPopulation = null;
            int bestDistance = Integer.MAX_VALUE;

            for (boolean[][] person : population) {
                IsValidFeedback isValidFeedback = isValidSolution(person, cities, citiesRequested);
                System.out.println("Validou");

                if (isValidFeedback.isValid) {
                    System.out.println("Valido: " + isValidFeedback.visited + " | " + isValidFeedback.distance);
                    validPopulation.add(isValidFeedback);
                    if (isValidFeedback.distance < bestDistance) {
                        bestDistance = isValidFeedback.distance;
                        bestOfPopulation = isValidFeedback;
                    }
                }
            }

            if (!validPopulation.isEmpty() && bestOfPopulation != null) {
                if (best == null) {
                    best = bestOfPopulation;
                } else if (bestOfPopulation.distance < best.distance) {
                    best = bestOfPopulation;
                    rounds = 0;
                } else {
                    rounds++;
                }
            }

            ArrayList<boolean[][]> newPopulation = new ArrayList<>();

            for (int i = 0; i < POPULATION_SIZE; i++) {
                if (validPopulation.size() >= 2) {
                    ArrayList<IsValidFeedback> survives = new ArrayList<>();

                    for (int j = 0; j < 2; j++) {
                        survives.add(getFather(validPopulation));
                    }

                    boolean[][] children = new boolean[cities.length][cities.length];
                    boolean crossover = random.nextDouble() <= CROSSOVER_RATE;

                    if (crossover) {
                        int x = random.nextInt(cities.length);
                        int y = random.nextInt(cities.length);

                        for (int k = 0; k < x; k++) {
                            for (int l = 0; l < y; l++) {
                                children[x][l] = survives.getFirst().person[x][l];
                            }
                        }
                        for (int k = x; k < cities.length; k++) {
                            for (int l = y; l < cities.length; l++) {
                                children[x][l] = survives.getLast().person[x][l];
                            }
                        }
                    } else {
                        IsValidFeedback bestOfSurvives = getBest(survives.getFirst(), survives.getLast());
                        children = bestOfSurvives.person;
                    }

                    // mutation
                    for (int k = 0; k < children.length; k++) {
                        for (int l = 0; l < children.length; l++) {
                            if (random.nextDouble() <= MUTATION_RATE) {
                                children[k][l] = !children[k][l];
                            }
                        }
                    }

                    newPopulation.add(children);

                } else {
                    System.out.printf("População irá morrer, apenas %d sobrevivente.", validPopulation.size());
                    return;
                }
            }

            population = newPopulation;
        }
        System.out.println("----- END JOB -----");
        System.out.println("Fim: " + best.visited + " | " + best.distance);
        System.out.println(Arrays.deepToString(best.person));

    }


    private static IsValidFeedback isValidSolution(boolean[][] person, int[][] cities, int[] citiesRequested) {
        int startCity = citiesRequested[0];
        List<Integer> visited = new ArrayList<>();
        visited.add(startCity);
        int[] bestDistanceReference = {Integer.MAX_VALUE};
        IsValidFeedback result = explorePaths(person, cities, citiesRequested, startCity, visited, 0, bestDistanceReference);
        if (result == null){
            return new IsValidFeedback(person);
        }

        return result;
    }

    // backtracking
    private static IsValidFeedback explorePaths(boolean[][] person, int[][] cities, int[] citiesRequested,
                                                int currentCity, List<Integer> visited, int currentDistance, int[] bestDistanceReference) {
        int startCity = citiesRequested[0];

        // limitações

        if (currentDistance >= bestDistanceReference[0]) {
            return null;
        }

        if (visited.size() > cities.length + 1) {
            return null;
        }

        // caso base - fechou o ciclo
        if (currentCity == startCity && visited.size() > 1) {
            boolean allVisited = true;
            for (int city : citiesRequested) {
                if (!visited.contains(city)) {
                    allVisited = false;
                    break;
                }
            }
            IsValidFeedback feedback = new IsValidFeedback(person);
            feedback.visited = new ArrayList<>(visited);
            feedback.distance = currentDistance;
            feedback.isValid = allVisited;
            return feedback;
        }

        IsValidFeedback bestFeedback = null;

        for (int j = 0; j < person[currentCity].length; j++) {
            if (person[currentCity][j]) {

                if (j != startCity && visited.contains(j)) {
                    continue;
                }

                List<Integer> newVisited = new ArrayList<>(visited);
                newVisited.add(j);
                int newDistance = currentDistance + cities[currentCity][j];

                IsValidFeedback candidate = explorePaths(person, cities, citiesRequested, j, newVisited, newDistance, bestDistanceReference);
                if (candidate != null && candidate.isValid) {
                    if (bestFeedback == null || candidate.distance < bestFeedback.distance) {
                        bestFeedback = candidate;
                    }
                }
            }
        }
        return bestFeedback;
    }


    private static IsValidFeedback getFather(ArrayList<IsValidFeedback> population) {
        Random rand = new Random();
        IsValidFeedback rand1 = population.get(rand.nextInt(population.size()));
        IsValidFeedback rand2 = population.get(rand.nextInt(population.size()));

        return getBest(rand1, rand2);
    }

    private static IsValidFeedback getBest(IsValidFeedback p1, IsValidFeedback p2) {
        if (p1.distance >= p2.distance) {
            return p1;
        }
        return p2;
    }

    private static int[] parseCities(String input) {
        String[] parts = input.trim().split("\\s+");
        int[] cities = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            cities[i] = Integer.parseInt(parts[i]);
        }
        return cities;
    }

}