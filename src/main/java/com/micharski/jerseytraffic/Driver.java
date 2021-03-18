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

import java.util.*;
import java.util.function.BiFunction;

public class Driver extends Application {

    static BiFunction<Point2D, Point2D, Double> pointDistance = (point1, point2) -> Math.sqrt(Math.pow(point2.getY() - point1.getY(), 2) + Math.pow(point2.getX() - point1.getX(), 2));
    //BiFunction<Point2D, Point2D, Double> getSlope = (point1, point2) -> (point2.getY() - point1.getY())/(point2.getX() - point1.getX());

    static ArrayList<Point2D> points;
    static int width = 600;
    static int height = 600;
    static int pointQuantity = 10;
    Stage primaryStage;

    KeyCombination regen = new KeyCodeCombination(KeyCode.ENTER);

    public static void generatePoints(Group root){
        points = new ArrayList<>();
        for(int i = 0; i < pointQuantity; i++){
            int newX = new Random().nextInt(width);
            int newY = new Random().nextInt(height);
            Point2D point = new Point2D(newX, newY);
            Circle circle = new Circle(newX, newY, 5);
            root.getChildren().add(circle);
            points.add(point);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        Group root = new Group();
        generatePoints(root);
        Scene scene = new Scene(root, width, height);
        convexHullGiftWrapping(root, scene);
        setStage(primaryStage, root, scene);
//        Point2D firstPoint = points.get(new Random().nextInt(pointQuantity-1));
//        for(int i = 0; i < pointQuantity*2; i++){
//            try {
//                Point2D nextPoint = findNextBorderPoint(firstPoint);//findNearestPoint(firstPoint);
//                Line line = new Line(firstPoint.getX(), firstPoint.getY(), nextPoint.getX(), nextPoint.getY());
//                root.getChildren().add(line);
//                points.remove(firstPoint);
//                firstPoint = nextPoint;
//            } catch(NullPointerException ex){
//                System.out.println("null pointer");
//                break;
//                //ex.printStackTrace(System.out);
//            }
////            Point2D nextPoints[] = findNearestPoints(firstPoint, 2);//findNearestPoint(firstPoint, 1, Math.PI/4);
////            for(Point2D p : nextPoints){
////                Line line = new Line(firstPoint.getX(), firstPoint.getY(), p.getX(), p.getY());
////                root.getChildren().add(line);
////                points.remove(firstPoint);
////                firstPoint = p;
////            }
//        }
    }

    public void setStage(Stage stage, Group root, Scene scene){
        scene.getAccelerators().put(regen, regenScene);
        stage.setScene(scene);
        stage.setTitle("Jersey Traffic");
        stage.show();
    }

    public Point2D findNearestPoint(Point2D point){
        double minDistance = -1;
        Point2D closestPoint = new Point2D(0, 0);
        for(Point2D p : points){
            if(pointDistance.apply(point, p) < minDistance || minDistance == -1){
                minDistance = pointDistance.apply(point, p);
                closestPoint = p;
            }
        }
        return closestPoint;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Point2D[] findNearestPoints(Point2D point, int quantity){
        MinMaxPriorityQueue<Point2D> closestPoints = MinMaxPriorityQueue.orderedBy(Comparator.comparing(p -> pointDistance.apply((Point2D) p, point)))
                .maximumSize(quantity)
                .create();
        closestPoints.addAll(points);
        return closestPoints.toArray(new Point2D[closestPoints.size()]);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Point2D findNextBorderPoint(Point2D point){
            final int _MAXSIZE = 10;
            if (point.getX() / width > 0.5) {
                MinMaxPriorityQueue<Point2D> closestPoints;
                if (point.getY() / height > 0.5) {
                    //4 = up
                    System.out.println("4th quadrant");
                    closestPoints = MinMaxPriorityQueue.orderedBy(Comparator.comparing(Point2D::getY))
                            .maximumSize(1)
                            .create();
                } else {
                    //1 = left
                    System.out.println("1st quadrant");
                    closestPoints = MinMaxPriorityQueue.orderedBy(Comparator.comparing(Point2D::getX).reversed())
                            .maximumSize(1)
                            .create();
                }
                closestPoints.addAll(Arrays.asList(findNearestPoints(point, _MAXSIZE)));
                System.out.println(closestPoints.size());
                return closestPoints.peekFirst();
            } else {
                //3 = right
                System.out.println("3rd quadrant");
                MinMaxPriorityQueue<Point2D> closestPoints;
                if (point.getY() / height > 0.5) {
                    closestPoints = MinMaxPriorityQueue.orderedBy(Comparator.comparing(Point2D::getX))
                            .maximumSize(1)
                            .create();
                } else {
                    //2 = down
                    System.out.println("2nd quadrant");
                    closestPoints = MinMaxPriorityQueue.orderedBy(Comparator.comparing(Point2D::getY).reversed())
                            .maximumSize(1)
                            .create();
                }
                closestPoints.addAll(Arrays.asList(findNearestPoints(point, _MAXSIZE)));
                //System.out.println(closestPoints.size());
                return closestPoints.peekFirst();
            }
        //find quadrant
        //1 = left
        //2 = down
        //3 = right
        //4 = up
    }

    public void drawDistrict(Group root){
        try {
            List<Point2D> points = FixedSizeList.fixedSizeList(Arrays.asList(new Point2D[3]));
            int i = new Random().nextInt(Driver.points.size()-1);
            int j = new Random().nextInt(Driver.points.size()-1);
            int k = new Random().nextInt(Driver.points.size()-1);
            while(i == j || j == k || i == k){
                i = new Random().nextInt(Driver.points.size()-1);
                j = new Random().nextInt(Driver.points.size()-1);
                k = new Random().nextInt(Driver.points.size()-1);
            }
            //kill me
            points.set(0, Driver.points.get(i)); //if this gives an index out of range exception, just subtract the index by 1
            points.set(1, Driver.points.get(j));
            points.set(2, Driver.points.get(k));
            Line line1 = new Line(points.get(0).getX(), points.get(0).getY(), points.get(1).getX(), points.get(1).getY());
            Line line2 = new Line(points.get(2).getX(), points.get(2).getY(), points.get(1).getX(), points.get(1).getY());
            Line line3 = new Line(points.get(0).getX(), points.get(0).getY(), points.get(2).getX(), points.get(2).getY());
            root.getChildren().add(line1);
            root.getChildren().add(line2);
            root.getChildren().add(line3);
            //Enhance it (do djkstra around it)
            int choice = new Random().nextInt(2); //0 = concave, 1 = right through, 2 = convex

        } catch (Exception ex){
            ex.printStackTrace(System.out);
        }
    }

    public void convexHullGiftWrapping(Group root, Scene scene){
        for(int i = 0; i < points.size(); i++){
            for(int j = i+1; j < points.size(); j++){
                boolean valid = true;
                for(int k = 0; k < points.size(); k++){
                    if(i != j && j != k && i != k){
                        if(points.get(k).getX() < points.get(j).getX() && points.get(k).getX() < points.get(i).getX()){
                            valid = false;
                            break;
                        }
                    }
                }
                if(valid){
                    root.getChildren().add(new Line(points.get(i).getX(), points.get(i).getY(), points.get(j).getX(), points.get(j).getY()));
                }
            }
        }
    }

    public void convexHull(Group root, Scene scene) {
            ArrayList<Point2D> hull = new ArrayList<>();
            Point2D leftMost = getLeftmostPoint();
            hull.add(leftMost);
            Point2D currentVertex = leftMost;
            Point2D nextVertex = points.get(1);
            int index = 2;
            Line currentLine = new Line(currentVertex.getX(), currentVertex.getY(), nextVertex.getX(), nextVertex.getY());
            Point2D checking = points.get(index);
            Line checkingLine = new Line(currentVertex.getX(), currentVertex.getY(), checking.getX(), checking.getY());
            while(index != points.size()) {
                currentLine = new Line(currentVertex.getX(), currentVertex.getY(), nextVertex.getX(), nextVertex.getY());
                checking = points.get(index);
                checkingLine = new Line(currentVertex.getX(), currentVertex.getY(), checking.getX(), checking.getY());
                checkingLine.setStroke(Color.RED);
                if (getCrossProduct(new Line(currentVertex.getX(), currentVertex.getY(), nextVertex.getX(), nextVertex.getY()),
                        new Line(currentVertex.getX(), currentVertex.getY(), checking.getX(), checking.getY())) < 0) {
                    nextVertex = checking;
                }
                index++;
            }
            hull.add(nextVertex);
            root.getChildren().add(new Circle(nextVertex.getX(), nextVertex.getY(), 5, Color.AQUA));
            root.getChildren().add(new Line(hull.get(0).getX(), hull.get(0).getY(), hull.get(1).getX(), hull.get(1).getY()));
            //root.getChildren().add(currentLine);
            //root.getChildren().add(checkingLine);
    }

    public double getCrossProduct(Line a, Line b){
        double aStartX = a.getStartX();
        a.setStartX(0);
        a.setEndX(a.getEndX() - aStartX);
        double aStartY = a.getStartY();
        a.setStartY(0);
        a.setEndY(a.getEndY() - aStartY);
        double bStartX = b.getStartX();
        b.setStartX(0);
        b.setEndX(b.getEndX() - bStartX);
        double bStartY = b.getStartY();
        b.setStartY(0);
        b.setEndY(b.getEndY() - bStartY);
        return (a.getEndX() * b.getEndY()) - (a.getEndY() * b.getStartX());
    }

    public Point2D getLeftmostPoint() {
        points.sort(Comparator.comparing(Point2D::getX));
        return points.get(0);
    }

    public void connectTheDots(Group root){
        points.forEach(p -> {
           points.forEach(q -> {
              if(!p.equals(q)){
                   root.getChildren().add(new Line(p.getX(), p.getY(), q.getX(), q.getY()));
              }
           });
        });
    }

    Runnable regenScene = () -> {
            start(primaryStage); //fix this later
    };

}