package com.micharski.jerseytraffic;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

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

    BiFunction<Point2D, Point2D, Double> slope = (a, b) -> (b.getY() - a.getY())/(b.getX() - a.getX());
    BiFunction<Point2D, Point2D, Integer> slopeY = (a, b) -> (int) (b.getY() - a.getY());

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

    //TODO: FIX THIS
    //Bentley-Ottman Algorithm
    public void markIntersections(Group root){
        PriorityQueue<Point2D> eventPoints = new PriorityQueue<>(points.size() * 2, Comparator.comparingDouble(Point2D::getX));
        TreeMap<Point2D, Point2D> crossers = new TreeMap<>(Comparator.comparingDouble(Point2D::getY));
        Line sweepLine = new Line(5, 0, 5, width+MARGIN_WIDTH*2);
        sweepLine.setStroke(Color.ORANGE);
        root.getChildren().add(sweepLine);
        eventPoints.addAll(points);
        for(int x = (int) eventPoints.peek().getX(); x < width + MARGIN_WIDTH*2; x++){
            final Point2D p = eventPoints.peek();
            lines.forEach(l -> {
                //check if p is the left endpoint of l, then add it to the treemap
                if(l.getStartX() < l.getEndX()){
                    if(p.getX() == l.getStartX() && p.getY() == l.getStartY()){
                        crossers.put(new Point2D(l.getStartX(), l.getStartY()), new Point2D(l.getEndX(), l.getEndY()));
                    }
                } else {
                    if(p.getX() == l.getEndX() && p.getY() == l.getEndY()){
                        crossers.put(new Point2D(l.getEndX(), l.getEndY()), new Point2D(l.getStartX(), l.getStartY()));
                    }
                }
                //check if p is the right endpoint of l, then remove it from the treemap
                if(l.getStartX() > l.getEndX()){
                    if(p.getX() == l.getStartX() && p.getY() == l.getStartY()){
                        crossers.remove(new Point2D(l.getStartX(), l.getStartY()), new Point2D(l.getEndX(), l.getEndY()));
                        eventPoints.remove(eventPoints.peek());
                    }
                } else {
                    if(p.getX() == l.getEndX() && p.getY() == l.getEndY()){
                        crossers.remove(new Point2D(l.getEndX(), l.getEndY()), new Point2D(l.getStartX(), l.getStartY()));
                        eventPoints.remove(eventPoints.peek());
                    }
                }
                //update each and every value in crossers
                crossers.forEach((q, r) -> {
                    if(l.getStartX() < l.getEndX()){
                        if(p.getX() == l.getStartX() && p.getY() == l.getStartY()){
                            q.add(1, slope.apply(new Point2D(l.getStartX(), l.getStartY()), new Point2D(l.getEndX(), l.getEndY())) * slopeY.apply(new Point2D(l.getStartX(), l.getStartY()), new Point2D(l.getEndX(), l.getEndY())));
                        }
                    } else {
                        if(p.getX() == l.getEndX() && p.getY() == l.getEndY()){
                            q.add(1, slopeY.apply(new Point2D(l.getEndX(), l.getEndY()), new Point2D(l.getStartX(), l.getStartY())) * slopeY.apply(new Point2D(l.getEndX(), l.getEndY()), new Point2D(l.getStartX(), l.getStartY())));
                        }
                    }
                });
            });
            Set<Object> newPoints = new HashSet<>();
            for(Point2D q : crossers.values()){
                if(!newPoints.add(q)){
                    System.out.println("Point of Intersection: " + q);
                }
            }
        }
    }

//    public void markIntersections(Group root){
//        ArrayList<Double> slopes = new ArrayList<>();
//        ArrayList<Line> activeLines = new ArrayList<>();
//        ArrayList<Point2D> currentPoint = new ArrayList<>();
//        int min = width + MARGIN_WIDTH*2;
//        for(Line l : lines) {
//            double s;
//            if(l.getStartX() < l.getEndX()){
//                s = slope.apply(new Point2D(l.getStartX(), l.getStartY()), new Point2D(l.getEndX(), l.getEndY()));
//                if(l.getStartX() < min){
//                    min = (int) l.getStartX();
//                }
//            } else {
//                s = slope.apply(new Point2D(l.getEndX(), l.getEndY()), new Point2D(l.getStartX(), l.getStartY()));
//                if(l.getEndX() < min){
//                    min = (int) l.getEndX();
//                }
//            }
//            slopes.add(s);
//        };
//        Line sweepLine = new Line(min, 0, min, width+MARGIN_WIDTH*2);
//        sweepLine.setStroke(Color.ORANGE);
//        root.getChildren().add(sweepLine);
//        for (int i = (int) sweepLine.getStartX(); i < height + MARGIN_WIDTH*2 - 5; i++) {
//            sweepLine.setStartX(i);
//            sweepLine.setEndX(i);
//            final int x = i;
//            lines.forEach(l -> {
//                if(l.getStartX() < l.getEndX()){
//                    if(l.getStartX() == x){
//                        activeLines.add(l);
//                    }
//                } else {
//                    if(l.getEndX() == x){
//                        activeLines.add(l);
//                    }
//                }
//                if(x > l.getStartX() && x > l.getEndX()){
//                    activeLines.remove(l);
//                }
//            });
//            for(int j = 0; j < )
//            activeLines.forEach(l -> {
//                if(l.getStartX() < l.getEndX()){
//
//                } else {
//
//                }
//            });
//        }
//    }

    Runnable regenScene = () -> {
            start(primaryStage); //fix this later
    };

}