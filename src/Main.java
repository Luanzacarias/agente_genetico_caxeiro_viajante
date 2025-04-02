import java.util.*;

public class Main {
    public static void main(String[] args) {
        int POPULATION_SIZE = 3000;
        int ROUNDS_WITHOUT_CHANGE = 10;
        double CROSSOVER_RATE = 0.35;
        double MUTATION_RATE = 0.25;
        final Random random = new Random();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Digite as cidades solicitadas separadas por espaço (ex.: 6 0 3):");
        String input = scanner.nextLine();
        int[] citiesRequested = parseCities(input);
        if (citiesRequested.length == 0) {
            System.out.println("Deve solicitar pelo menos uma cidade");
            return;
        }

        int[][] cities = FileService.readInputFile("cidades.txt");
        if (cities == null) {
            System.out.println("Erro ao abrir arquivo");
            return;
        }


        if (cities.length < citiesRequested.length) {
            System.out.println("Mais cidades solicitadas do que as disponíveis");
            return;
        }

        // start population
        ArrayList<boolean[][]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            boolean[][] person = new boolean[cities.length][cities.length];
            for (int j = 0; j < cities.length; j++) {
                for (int k = 0; k < cities.length; k++) {
                    person[j][k] = random.nextDouble() >= 0.4;
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
                                children[k][l] = survives.getFirst().person[k][l];
                            }
                        }
                        for (int k = x; k < cities.length; k++) {
                            for (int l = y; l < cities.length; l++) {
                                children[k][l] = survives.getLast().person[k][l];
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
                    System.out.printf("População irá morrer, apenas %d sobreviventes.", validPopulation.size());
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
        // A cidade inicial é a primeira da lista de cidades solicitadas

        IsValidFeedback isValidFeedback = new IsValidFeedback(person);

        int startCity = citiesRequested[0];
        isValidFeedback.visited.add(startCity);
        int currentCity = startCity;

        // Percorre a matriz a partir da cidade atual até voltar ao início
        while (true) {
            int nextCity = -1;
            // Procura a primeira cidade conectada (valor true) na linha da cidade atual
            for (int j = 0; j < person[currentCity].length; j++) {
                if (person[currentCity][j]) {
                    nextCity = j;
                    break;
                }
            }

            // Se não encontrou conexão, encerra sem sucesso
            if (nextCity == -1) {
                break;
            }

            // Se já encontrou a cidade inicial, fecha o ciclo
            if (nextCity == startCity) {
                isValidFeedback.distance += cities[currentCity][nextCity];
                isValidFeedback.visited.add(nextCity);
                break;
            }

            // Se a cidade já foi visitada, o caminho se repete e não é válido
            if (isValidFeedback.visited.contains(nextCity)) {
                break;
            }

            // Adiciona a cidade e continua a partir dela
            isValidFeedback.distance += cities[currentCity][nextCity];
            isValidFeedback.visited.add(nextCity);
            currentCity = nextCity;
        }

        // Verifica se o ciclo foi fechado (última cidade é a inicial)
        if (isValidFeedback.visited.getLast() != startCity) {
            return isValidFeedback;
        }

        // Verifica se todas as cidades solicitadas foram visitadas durante o percurso
        for (int city : citiesRequested) {
            if (!isValidFeedback.visited.contains(city)) {
                return isValidFeedback;
            }
        }

        isValidFeedback.isValid = true;
        return isValidFeedback;
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