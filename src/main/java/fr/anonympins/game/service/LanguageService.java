package fr.anonympins.game.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.anonympins.game.model.NeuralNetwork;
import fr.anonympins.game.utils.FileUtils;
import lombok.Data;
import org.deeplearning4j.optimize.listeners.PerformanceListener;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.iter.NdIndexIterator;
import org.nd4j.linalg.api.ops.impl.indexaccum.custom.ArgMax;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.LowCasePreProcessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.RmsProp;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class LanguageService extends ApiCallService {

    List<String> grammarList = List.of(
            "nom commun",
            "nom propre",
            "verbe infinitif",
            "verbe participe passé",
            "verbe participe présent",
            "verbe conjugue 1s",
            "verbe conjugue 2s",
            "verbe conjugue 3s",
            "verbe conjugue 1p",
            "verbe conjugue 2p",
            "verbe conjugue 3p",
            "determinant"
    );

    List<Character> charList = List.of('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', ' ', '!', '?', ',', ';',
            ':', '(', ')','{', '}', '<', '>', '[', ']',
            'é', 'à', 'ç', '\'', '"', '+', '-', '/', '*', '=', '.');

    private static final Integer EMBEDDING_SIZE = 100;
    private static final Integer EMBEDDING_NB_WORDS = 4; // min 2 for now

    Model model;

    private List<String> propositions = new ArrayList<>();

    NeuralNetwork neuralNetwork;
    TokenizerFactory tokenizerFactory;
    MultiLayerNetwork languageModel;

    LanguageService(){
        neuralNetwork = new NeuralNetwork(20*35, 18, propositions.size());

        // Create a DefaultTokenizerFactory with pre-processing steps
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor()); // Apply common pre-processing
        tokenizerFactory.setTokenPreProcessor(new LowCasePreProcessor()); // Convert to lowercase (optional)

        initModel();
    }

    public void initModel(){
        try {
            languageModel = MultiLayerNetwork.load(new File("data/modele_llm.dat"), true);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void generateModel() throws IOException {

        trainWordModel();
        languageModel = createGrammarModel();
    }

    @Data
    public static class ModelLine {
        @JsonProperty("a")
        private String request;
        @JsonProperty("b")
        private String response;
    }
    @Data
    public static class Model {
        private List<ModelLine> lines;
    }

    public void saveModel(Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        saveFile("model.json", mapper.writeValueAsString(model));
        this.model = model;
    }

    public void saveFile(String filename, String content) throws IOException {
        FileWriter myWriter = new FileWriter(filename);
        myWriter.write(content);
        myWriter.close();
    }

    public Mono<String> getText() {
        return get("https://fr.wikipedia.org/wiki/Spécial:Page_au_hasard", String.class)
                .map(m -> {
                    Document doc = Jsoup.parse(m);
                    String s = "";
                    for (Element e : doc.body().select("#mw-content-text p")) {
                        s += e.text();
                    }
                    return s;
                });
    }

    public Mono<Model> getModel(){
        ObjectMapper mapper = new ObjectMapper();
        Model model = null;
        try {
            model = mapper.readValue(FileUtils.readFile("model.json"), Model.class);
        } catch (IOException e) {
            return Mono.error(new RuntimeException(e));
        }
        List<ModelLine> lines = model.getLines();

        Model finalModel = model;
        return getText().flatMap(text -> {
//            "bonjour je veux etre enregistré"
            var arr = splitIntoWords(text);
            List<String> words = new ArrayList<>();
            for(int i = 0; i < arr.length - 1; i++) {
                String word = arr[i];
                words.add(word);
                String chars = "";
                for (int j = 0; j < arr[i+1].length(); ++j ) {
                    ModelLine modelLine = new ModelLine();
                    modelLine.setRequest(String.join(" ", words)+chars);
                    modelLine.setResponse(arr[i+1].charAt(j)+"");

                    char c = arr[i+1].charAt(j);
                    chars += c;

                    lines.add(modelLine);
                }
            }

            finalModel.setLines(lines);

            try {
                saveModel(finalModel);
                return Mono.just(finalModel);
            } catch (IOException e) {
                return Mono.error(e);
            }
        });
    }

    public Mono<String> getNextToken(String text){

        WordVectors wordVectors = WordVectorSerializer.readWord2VecModel(new File("data/modele_word2vec.txt"));

        // Tokenize the input text
        Tokenizer tokenizer = tokenizerFactory.create(text);

        // Créer une liste pour stocker les mots du vocabulaire
        List<INDArray> embeddings = new ArrayList<>();

        while (tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            INDArray embedding = Nd4j.create(wordVectors.getWordVector(token));
            embedding.add(embedding);
        }
        INDArray inputData = Nd4j.vstack(embeddings.toArray(new INDArray[0]));

        INDArray output = languageModel.output(inputData);  // Supposons que trainedModel est votre modèle déjà entraîné
        int indiceTokenPlusProbable = Nd4j.argMax(output, 1).getInt(0);

// Convertissez l'indice en token à l'aide de votre vocabulaire ou tokenizer
        String tokenPlusProbable = tokenizer.getTokens().get(indiceTokenPlusProbable);
        System.out.println("token le plus probable : " + tokenPlusProbable);

        return Mono.just(tokenPlusProbable);
    }

    public void trainWordModel(){
        System.out.println("Training from corpus...");
        // Spécifiez le chemin vers votre corpus
        File corpusFile = new File("data/words.txt");

        Word2Vec word2Vec = new Word2Vec.Builder()
                .minWordFrequency(1)
                .layerSize(EMBEDDING_SIZE) // Taille des embeddings
                .seed(42)
                .windowSize(5)
                .iterate(new FileSentenceIterator(corpusFile)) // Iterable<String> contenant vos données textuelles
                .tokenizerFactory(tokenizerFactory)
                .build();
        // Entraîner le modèle Word2Vec
        word2Vec.fit();

        // Sauvegarder le modèle entraîné
        WordVectorSerializer.writeWord2VecModel(word2Vec, new File("data/modele_word2vec.txt"));

        System.out.println("Word2Vec initialized.");
    }

    public MultiLayerNetwork createLLMModel() throws IOException {

        int HIDDEN_LAYER_WIDTH = 100;

        var conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .biasInit(0)
                .miniBatch(false)
                .updater(new RmsProp(0.001))
                .weightInit(WeightInit.XAVIER)
                .dataType(DataType.FLOAT)
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(charList.size())
                        .nOut(HIDDEN_LAYER_WIDTH)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new LSTM.Builder()
                        .nIn(HIDDEN_LAYER_WIDTH)
                        .nOut(HIDDEN_LAYER_WIDTH)
                        .activation(Activation.TANH)
                        .build())
                .layer(2, new LSTM.Builder()
                        .nIn(HIDDEN_LAYER_WIDTH)
                        .nOut(HIDDEN_LAYER_WIDTH)
                        .activation(Activation.TANH)
                        .build())
                .layer(3, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nIn(HIDDEN_LAYER_WIDTH)
                        .nOut(charList.size())
                        .build())
//                .layer(new DenseLayer.Builder().nIn(nIn).nOut(nIn).activation(Activation.RELU).build())
                //.layer(new SelfAttentionLayer.Builder().nIn(nIn).nOut(nIn).nHeads(1).build())
                //.layer(new LSTM.Builder().nIn(nIn).nOut(nOut)
                //        .activation(Activation.TANH).build())
                //.layer(new LSTM.Builder().nIn(nOut).nOut(nOut)
                //        .activation(Activation.TANH).build())
                //.layer(new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                //        .activation(Activation.SOFTMAX).nIn(nOut).nOut(nOut).build())
                //.layer(new LearnedSelfAttentionLayer.Builder().nIn(hiddenLayers).nOut(hiddenLayers).nHeads(1).nQueries(3).build())
                //.layer(new RecurrentAttentionLayer.Builder().nIn(hiddenLayers).nOut(hiddenLayers).nHeads(1).projectInput(false).hasBias(false).build())
                //.layer(new GlobalPoolingLayer.Builder().poolingType(PoolingType.MAX).build())
                // .layer(new OutputLayer.Builder().nOut(nOut).activation(Activation.SOFTMAX)
                //       .lossFunction(LossFunctions.LossFunction.MCXENT).build())
                //.setInputType(InputType.recurrent(nIn))
                .build();


        // Create a MultiLayerNetwork from the configuration
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init(); // Initialize the model
        System.out.println("LLM model initialized.");

        model.save(new File("data/modele_llm.dat"));
        System.out.println("LLM model Saved.");

        //        model.setListeners(new PerformanceListener(1));
        return model;
    }
    public MultiLayerNetwork createGrammarModel() throws IOException {

        int INPUT_LAYER_WIDTH = EMBEDDING_SIZE;
        int HIDDEN_LAYER_WIDTH = 100;

        var conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .updater(new Adam(0.001))
                .weightInit(WeightInit.XAVIER)
                .dataType(DataType.FLOAT)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(INPUT_LAYER_WIDTH)
                        .nOut(HIDDEN_LAYER_WIDTH)
                        .activation(Activation.TANH)
                        .build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nIn(HIDDEN_LAYER_WIDTH)
                        .nOut(grammarList.size())
                        .build())
                .build();


        // Create a MultiLayerNetwork from the configuration
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init(); // Initialize the model
        System.out.println("Grammar model initialized.");

        model.save(new File("data/modele_llm.dat"));
        System.out.println("LLM model Saved.");

        //        model.setListeners(new PerformanceListener(1));
        return model;
    }

    public void registerText(String text) throws IOException {

        // Spécifiez le chemin vers votre corpu
        BufferedWriter writer = new BufferedWriter(new FileWriter("data/corpus.txt"));
        writer.write(text);
        writer.close();

        // Créer un SentenceIterator pour lire le corpus
        File corpusFile = new File("data/corpus.txt");
        FileSentenceIterator sentenceIterator = new FileSentenceIterator(corpusFile);
        while (sentenceIterator.hasNext()) {
            String sentence = sentenceIterator.nextSentence();
            if(sentence == null || sentence.length() == 0)
                break;

            var maxLength = 50;
            var sentenceLength = Math.min(maxLength, sentence.length());
            var input = Nd4j.zeros(1, charList.size(), sentenceLength);
            var labels = Nd4j.zeros(1, charList.size(), sentenceLength);
// loop through our sample-sentence
            System.out.println(input.shapeInfoToString());

            int init = (int)(Math.random()*sentenceLength);
            for(int samplePos = init; samplePos < Math.min(init+maxLength, sentenceLength); ++samplePos) {
 //               String chunk = sentence.substring(c, c+20 > sentence.length() ? sentence.length() - 1 : c+20);

                    // small hack: when currentChar is the last, take the first char as
                    // nextChar - not really required. Added to this hack by adding a starter first character.
                    var currentChar = sentence.charAt(samplePos);
                    // On the last character we point back to the first character as next position
                    var nextChar = sentence.charAt((samplePos + 1) % (sentence.length()));
                    // input neuron for current-char is 1 at "samplePos"
                    input.putScalar(new int[]{0, charList.indexOf(currentChar), samplePos}, 1);
                    // output neuron for next-char is 1 at "samplePos"
                    labels.putScalar(new int[]{0, charList.indexOf(nextChar), samplePos}, 1);
                }

            for(int j = 0; j < 200; ++j)
                languageModel.fit(input, labels);

            /*
            List<INDArray> finalArr = new ArrayList<>();
            for(int i = 0; i < tokens.size() - 1; ++i){
                finalArr.add(wordVectors.getWordVectorMatrix(tokens.get(i)));
                if( finalArr.size() > 10 )
                    finalArr.remove(0);
                List<INDArray> indArray = new ArrayList<>(finalArr);
                for(int j = indArray.size(); j < 10; ++j){
                    indArray.add(Nd4j.zeros(100));
                }

                INDArray input = Nd4j.zeros(1, 1000);
                input.addi(Nd4j.concat(0, indArray.toArray(new INDArray[0])));

                INDArray output = Nd4j.zeros(1, 100);

                var outputToken = tokens.get(i+1);
                output.addi(wordVectors.getWordVectorMatrix(outputToken));

                model.fit(input, output);
            }

             */
        }

        System.out.println("Model trained with data/corpus.txt");
    }


    public static INDArray getPositionalEmbeddings(int sequenceLength, int embeddingSize) {
        INDArray positionalEmbeddings = Nd4j.create(sequenceLength, embeddingSize);

        for (int pos = 0; pos < sequenceLength; pos++) {
            for (int i = 0; i < embeddingSize; i++) {
                double angle = pos / Math.pow(10000, 2.0 * i / embeddingSize);
                positionalEmbeddings.putScalar(pos, i, Math.sin(angle));
            }
        }

        return positionalEmbeddings;
    }

    public static INDArray concatenateWordAndPositionalEmbeddings(INDArray wordEmbeddings, INDArray positionalEmbeddings) {
        int sequenceLength = EMBEDDING_NB_WORDS;
        int embeddingSize = EMBEDDING_SIZE;

        INDArray concatenatedEmbeddings = Nd4j.create(new int[]{sequenceLength, 2 * embeddingSize});
        for (int pos = 0; pos < sequenceLength; pos++) {
            concatenatedEmbeddings.putRow(pos, Nd4j.concat(0, wordEmbeddings.getRow(pos), positionalEmbeddings.getRow(pos)));
        }
        return concatenatedEmbeddings;
    }

    public String predictGrammar(String word) {


        WordVectors wordVectors = WordVectorSerializer.readWord2VecModel(new File("data/modele_word2vec.txt"));

        INDArray input = Nd4j.zeros(1, EMBEDDING_SIZE);
        input.addi(wordVectors.getWordVectorMatrix(word));

        INDArray output = languageModel.output(input);
        int index = output.getInt(0);
        return grammarList.get(index);
    }

    public String predictToken(String sentence){

        /*
        // clear current stance from the last example
        languageModel.rnnClearPreviousState();

// put the first character into the rrn as an initialisation
        var testInit = Nd4j.zeros(1, charList.size(), sentence.length());

// Remplissez la séquence avec les caractères initiaux de votre texte
        for (int i = 0; i < sentence.length(); ++i) {
            var pos = charList.indexOf(sentence.charAt(i));
            testInit.putScalar(new int[]{0, pos, i}, 1);
        }

// run one step -> IMPORTANT: rnnTimeStep() must be called, not
// output(). The output shows what the net thinks what should come next
        var output = languageModel.rnnTimeStep(testInit);
        var result = "";
        for (int s = 0; s < 255; ++s){
            // first process the last output of the network to a concrete
            // neuron, the neuron with the highest output has the highest
            // chance to get chosen

            double max = 0d;
            int sampledCharacterIdx = -1;
            for (int i = 0; i < charList.size(); ++i){
                var v = output.getDouble(1, i);
                if( v > max ){
                    max = v;
                    sampledCharacterIdx = i;
                }
            }

            // concatenate the chosen output
            result += charList.get(sampledCharacterIdx);

            // use the last output as input
            var nextInput = Nd4j.zeros(1, charList.size(), 1);
            nextInput.putScalar(sampledCharacterIdx, 1);
            output = languageModel.rnnTimeStep(nextInput);
        }

        List<String> tokens = tokenizer.getTokens();
        List<INDArray> arr = new ArrayList<>();
        List<String> toks = new ArrayList<>();

        // parcourt chaque sentence et chaqu mot fabrique un payload de 0
        // avec les 10 derniers tokens de la phrase.
        // si il y en a moins, on retourne le tableau paddé
        // comme ceci 0 0 0 0 0 0 0 0
        for( int i = 0; i < EMBEDDING_NB_WORDS; ++i){
            if( tokens.size() - EMBEDDING_NB_WORDS + i >= 0 ) {
                String token = tokens.get(tokens.size() - EMBEDDING_NB_WORDS + i);
                arr.add(wordVectors.getWordVectorMatrix(token));
                toks.add(token);
            }
        }
        for(int i = arr.size(); i < EMBEDDING_NB_WORDS; ++i){
            arr.add(Nd4j.zeros(EMBEDDING_SIZE));
            toks.add("");
        }

        INDArray input = Nd4j.zeros(1, EMBEDDING_NB_WORDS*EMBEDDING_SIZE);
        input.addi(Nd4j.concat(0, arr.toArray(new INDArray[0])));

        INDArray input1 = Nd4j.zeros(EMBEDDING_NB_WORDS, EMBEDDING_SIZE);
        int a = 0;
        for(INDArray ar : arr){
            input1.getRow(a).add(ar);
            ++a;
        }
        INDArray finalInput = Nd4j.zeros(1, EMBEDDING_SIZE*EMBEDDING_NB_WORDS*2);
        finalInput.getRow(0).add(Nd4j.toFlattened(concatenateWordAndPositionalEmbeddings(input1,
                getPositionalEmbeddings(EMBEDDING_NB_WORDS, EMBEDDING_SIZE))));

        INDArray output = languageModel.output(finalInput);

        // Trouvez le token associé à la plus grande probabilité (argmax)
        int maxProbIndex = Nd4j.argMax(output, 1).getInt(0);
        System.out.println("Shape of output: " +Nd4j.argMax(output, 0).shapeInfoToString());

        // Utilisez maxProbIndex pour récupérer le token du vocabulaire
        String predictedToken = wordVectors.vocab().wordAtIndex(maxProbIndex);
        return result;
*/
        return "";
    }

    public Mono<Model> trainModel() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return getModel().map(model -> {

            // on veut charger les propositions d'unvalidité si
            // par l'inverse de levenshtein
            double[][] data = new double[model.getLines().size()][20*35];
            double[][] answer = new double[model.getLines().size()][propositions.size()];

            int i = 0;
            model.getLines().forEach((modelLine) -> {
                data[i] = StringToDouble(modelLine.getRequest());
                List<Double> answers = new ArrayList<>();
                for(String p : propositions){
                    if (p.equals(modelLine.getResponse())) {
                        answers.add(1d);
                    } else {
                        answers.add(0d);
                    }
                }
                answer[i] = answers.stream().mapToDouble(Double::doubleValue).toArray();
            });

            int epochs = 1200;
            System.out.println("Fitting... ("+epochs+" epochs)");
            neuralNetwork.fit(data, answer, epochs);
            System.out.println("Done.");

            return model;
        });
        //Model model = new Model();

    }

    static int calculateLevenshtein(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                                    + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    public static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    public static int min(int... numbers) {
        return Arrays.stream(numbers)
                .min().orElse(Integer.MAX_VALUE);
    }

    String[] splitIntoWords(String str){
        return str.split("[ ?!:;/=.-]");
    }
    double wordsResemblance(String s, String s2){
        double e = 0;
        var arr1 = splitIntoWords(s);
        var arr2 = splitIntoWords(s2);
        for(String word1 : arr1){
            for(String word2: arr2){
                e += calculateLevenshtein(word1, word2);
            }
        }
        return e;
    }
    /*
    init = Je ne sais que moi. Je sais parler.
    prop = sais-tu parler ? apprend de moi. Ping
        */
    public void change_subjects_by_subjects(List<String> init, List<String> prop){
        // Init doit changer : car il y a un point d'interrogation ?
        // non, tout simplement meme sans point d'interrogation
        // savoir faire des propositions
        // matrice de ressemblance entre le corpus init et la phrase de

        /*
        for(String p : prop){
            String phrase = "";
            double min = Double.MAX_VALUE;
            for(String i : init){
                double d = wordsResemblance(p,i) / i.length();
                if(d < min){
                    min = d;
                    phrase = i;
                }
            }
            System.out.println("Phrase la plus ressemblante dans le corpus avec " + p);
            System.out.println(phrase);
            System.out.println(min);

        }*/

    }

    public void choosePropFromInput(String input){

    }

    // avantage : réponse la plus probable et la plus formelle, considere
    // les fautes de frappe.

    // Rseaux de neurones
    /*
    une facon de faire est de selectionner les mots qui suivent ou précédent dans la prop
    avantages: une réponse cohérente et "ressemblante" au corpus étudié

    une autre est d'avoir une liste de propositions predefinies
    et de sortir la version la plus probable selon une proposition d'entrée
    i:20*35 phrase stockée
    h:18 : nombre de dimensions d'apprentissage
    o:propositions predefinies

    35:capé à 0 0 0 0 0 0
    20:capé à 0 0 0 0 0 0

    // avantage une réponse harmonisée et probabiliste donc modifiable
    // un stockage limité de l'entrée et des propositions toute faites uniquement

    // REPONDRE AVEC STYLE :
    // pour repondre avec style, un deuxieme perceptron peut etre utilisé
    // pour sélectionner parmi les sorties du premier, celles qui satisferont le mieux le choix de l'utilisateur
    // a savoir la forme du texte d'entrée du premier perceptron
    // c'est a dire les features (en sortie du perceptron) qui seront appliqués
    // joie, tristesse, etc...

    // DONNEES D'ENTRAINEMENT ET MODELING
    // 1er perceptron : dialogue retranscrit, phrases crawlées.
    // 2eme perceptron : classifier les données selon une humeur plus que l'autre
    // éditeur des jauges en html/js
     */

    public double[] StringToDouble(String str){
        double[] dbl = new double[20*35];
        var arr= splitIntoWords(str);
        for(int j = 0; j < Math.min(arr.length, 20); ++j) {
            String word = arr[j];
            for (int i = 0; i < Math.min(35, word.length()); ++i) {
                dbl[j*35+i] = word.charAt(i);
            }
            for(int i = Math.min(35, word.length()); i < 35; ++i){
                dbl[j*35+i] = 0;
            }
        }
        return dbl;
    }


    public String getBestResponse(String request) {

        System.out.println("Loading data...");
        double[] data = StringToDouble(request);
        System.out.println("Done.");

        System.out.println("Predicting data...");
        List<Double> prediction = neuralNetwork.predict(data);
        String finalShortcut = "";
        double bestProb = 0;
        int i = 0;
        for(Double p : prediction){
            if( p > bestProb) {
                bestProb = p;
                finalShortcut = propositions.get(i);
            }
            ++i;
        }

        return finalShortcut;
    }

    int probRand(List<Double> predictions){
        Random r = new Random();
        int i = 0;
        for(Double p : predictions){
            var e = r.nextDouble(0, 1);
            if( e > p ){
                return i;
            }
            ++i;
        }
        return -1;
    }

}
