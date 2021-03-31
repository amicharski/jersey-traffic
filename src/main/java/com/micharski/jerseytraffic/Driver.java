package com.micharski.jerseytraffic;

import com.google.common.collect.MinMaxPriorityQueue;
import javafx.application.Application;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import org.apache.commons.collections4.list.FixedSizeList;
import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.collections4.set.ListOrderedSet;

import java.util.*;
import java.util.function.BiFunction;

public class Driver extends Application {

    static final int MARGIN_WIDTH = 100;
    static ArrayList<Point2D> points;
    static ArrayList<Line> lines;
    static int width = 600;
    static int height = 600;
    static int pointQuantity = 4;
    Stage primaryStage;

    KeyCombination regen = new KeyCodeCombination(KeyCode.ENTER);

    public static void generatePoints(Group root){
        points = new ArrayList<>();
        for(int i = 0; i < pointQuantity; i++){
            int newX = new Random().nextInt(width);
            int newY = new Random().nextInt(height);
            Point2D point = new Point2D(newX+MARGIN_WIDTH, newY+MARGIN_WIDTH);
            Circle circle = new Circle(newX+MARGIN_WIDTH, newY+MARGIN_WIDTH, 5);
            root.getChildren().add(circle);
            points.add(point);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            Group root = new Group();
            generatePoints(root);
            Scene scene = new Scene(root, width+(MARGIN_WIDTH*2), height+(MARGIN_WIDTH*2));
            drawRandomLines(root);
            //createConvexHull(root, primaryStage, scene);
            setStage(primaryStage, root, scene);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void setStage(Stage stage, Group root, Scene scene){
        scene.getAccelerators().put(regen, regenScene);
        stage.setScene(scene);
        stage.setTitle("Jersey Traffic");
        stage.show();
    }

    public void drawRandomLines(Group root){
        lines = new ArrayList<>();
        for(int i = 0; i < points.size()-1; i++){
            lines.add(new Line(points.get(i).getX(), points.get(i).getY(), points.get(i+1).getX(), points.get(i+1).getY()));
            i++;
        }
        root.getChildren().addAll(lines);
        markIntersections(root);
    }

    //TODO: marks intersections of two lines
    public void markIntersections(Group root){
        PriorityQueue<Point2D> eventPoints = new PriorityQueue<>();
        Line sweepLine = new Line(5, 0, 5, width+MARGIN_WIDTH);
        sweepLine.setStroke(Color.ORANGE);
        root.getChildren().add(sweepLine);
        //sweepLine.
    }

    public void createConvexHull(Group root, Stage stage, Scene scene){
        ListOrderedSet<Point2D> CH = convexHull(root, stage, scene);
        int avgX = 0;
        int avgY = 0;
        for(Point2D p : CH){
            avgX += p.getX();
            avgY += p.getY();
        }
        avgX /= CH.size();
        avgY /= CH.size();
        Point2D center = new Point2D(avgX, avgY);
        List<Point2D> newCH = new ArrayList<>();
        newCH.addAll(CH);
        newCH.sort((Comparator) (o1, o2) -> {
            Point2D a = (Point2D) o1;
            Point2D b = (Point2D) o2;
            if(a.getX() - center.getX() >= 0 && b.getX() - center.getX() < 0){
                return -1;
            }
            if(a.getX() - center.getX() < 0 && b.getX() - center.getX() >= 0){
                return 1;
            }
            return (int) ((a.getX() - center.getX()) * (b.getY() - center.getY()) - (b.getX() - center.getX()) * (a.getY() - center.getY()));
        });
        //root.getChildren().add(new Circle(avgX, avgY, 5, Color.RED));
        for(int i = 0; i < newCH.size(); i++){
            if(i+1 == newCH.size()){
                root.getChildren().add(new Line(newCH.get(i).getX(), newCH.get(i).getY(), newCH.get(0).getX(), newCH.get(0).getY()));
            } else {
                root.getChildren().add(new Line(newCH.get(i).getX(), newCH.get(i).getY(), newCH.get(i+1).getX(), newCH.get(i+1).getY()));
            }
        }
    }

    public ListOrderedSet<Point2D> convexHull(Group root, Stage stage, Scene scene) {
        ListOrderedSet returnValue = new ListOrderedSet();
        for(int i = 0; i < points.size(); i++){
            if(i == 0){
                root.getChildren().add(new Circle(points.get(0).getX(), points.get(0).getY(), 5, Color.ORANGE));
            }
            for(int j = i+1; j < points.size(); j++){
                if(j == 1){
                    root.getChildren().add(new Circle(points.get(1).getX(), points.get(1).getY(), 5, Color.BLUEVIOLET));
                }
                boolean valid = true;
                for(int k = 0; k < points.size(); k++){
                    if(i != j && j != k && i != k){
                        if(crossProduct(lineToVector(new Line(points.get(i).getX(), points.get(i).getY(), points.get(j).getX(), points.get(j).getY()), false),
                                lineToVector(new Line(points.get(i).getX(), points.get(i).getY(), points.get(k).getX(), points.get(k).getY()), false)) >= 0){
                            valid = false;
                            break;
                        }
                    }
                }
                if(valid){
                    returnValue.add(points.get(i));
                    returnValue.add(points.get(j));
                    //root.getChildren().add(new Line(points.get(i).getX(), points.get(i).getY(), points.get(j).getX(), points.get(j).getY()));
                }
            }
        }
        return returnValue;
    }

    public Vector<Double> lineToVector(Line line, boolean abs){
        Vector<Double> returnValue = new Vector<Double>(2, 0);
        if(abs){
            returnValue.add(Math.abs(line.getStartX() - line.getEndX()));
            returnValue.add(Math.abs(line.getStartY() - line.getEndY()));
        } else {
            returnValue.add(line.getStartX() - line.getEndX());
            returnValue.add(line.getStartY() - line.getEndY());
        }
        return returnValue;
    }

    public double crossProduct(Vector<Double> vectorA, Vector<Double> vectorB){
        return (vectorA.get(0)*vectorB.get(1))-(vectorA.get(1)*vectorB.get(0));
    }

    Runnable regenScene = () -> {
            start(primaryStage); //fix this later
    };

}