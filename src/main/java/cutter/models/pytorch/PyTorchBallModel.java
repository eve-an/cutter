package cutter.models.pytorch;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.BufferedImageFactory;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.translator.YoloV5Translator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import cutter.config.ConfigReader;
import cutter.geometry.MyBoundingBox;
import cutter.geometry.MyPoint;
import cutter.models.BallModel;
import cutter.scores.BallScore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;

public enum PyTorchBallModel implements BallModel {
    INSTANCE;

    private static final Logger log = LogManager.getLogger(PyTorchBallModel.class);
    private ImageFactory imageFactory;
    private ZooModel<Image, DetectedObjects> model;

    private final double IMAGE_SIZE = ConfigReader.getConfig().getBallmodel().getImageSize();
    private final String MODEL_PATH = ConfigReader.getConfig().getBallmodel().getPath();

    PyTorchBallModel() {
        try {
            init();
        } catch (IOException | ModelNotFoundException | MalformedModelException e) {
            System.err.println("Could not initialize PyTorch ball model.");
            throw new RuntimeException(e);
        }
    }

    private void init() throws IOException, ModelNotFoundException, MalformedModelException {
        Criteria<Image, DetectedObjects> criteria = createOnnx();
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);


        this.imageFactory = BufferedImageFactory.getInstance();
        this.model = model;
    }

    private Criteria<Image, DetectedObjects> createOnnx() throws MalformedURLException {
        YoloV5Translator translator = YoloV5Translator.builder()
                .optSynset(List.of("ball"))
                .build();

        return Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optTranslator(translator)
                .optModelPath(Paths.get(MODEL_PATH))
                .optDevice(Device.cpu())
                .optEngine("OnnxRuntime")
                .build();
    }

    @Override
    public void close() {
        this.model.close();
    }

    @Override
    public BallScore predict(Mat mat) {
        int origWidth = mat.width();
        int origHeight = mat.height();

        Imgproc.resize(mat, mat, new Size(IMAGE_SIZE, IMAGE_SIZE));
        Image img = imageFactory.fromImage(HighGui.toBufferedImage(mat));

        return processImage(img, origHeight, origWidth);
    }


    private BallScore processImage(Image image, int origHeight, int origWidth) {
        try {
            try (Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
                DetectedObjects objects = predictor.predict(image);
                DetectedObjects.DetectedObject ball = objects.best();
                return new BallScore(ball.getProbability(), parseBoundingBox(ball.getBoundingBox(), origHeight, origWidth));
            }
        } catch (NoSuchElementException e) {
            BallScore noBall = BallScore.GetDefault();
            noBall.setScore(0.0);
            return noBall;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    private MyBoundingBox parseBoundingBox(BoundingBox bbox, int height, int width) {
        Rectangle bounds = bbox.getBounds();

        double wScale = width / IMAGE_SIZE;
        double hScale = height / IMAGE_SIZE;

        int x = (int) (bounds.getX() * wScale);
        int y = (int) (bounds.getY() * hScale);

        int w = (int) (bounds.getWidth() * wScale);
        int h = (int) (bounds.getHeight() * hScale);

        MyPoint min = new MyPoint(x, y);
        MyPoint max = new MyPoint(x + w, y + h);
        return new MyBoundingBox(min, max);
    }

}
