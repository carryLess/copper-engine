/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.scoopgmbh.copper.monitoring.client.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class ComponentUtil {
	
	public static Node createProgressIndicator(){
		ProgressIndicator indicator = new ProgressIndicator();
		indicator.setPrefHeight(350);
		indicator.setPrefWidth(350);
		indicator.setMaxHeight(350);
		indicator.setMaxWidth(350);
		
		BorderPane borderPane = new BorderPane();
		BorderPane.setMargin(indicator, new Insets(5));
		borderPane.setCenter(indicator);
		borderPane.setStyle("-fx-background-color: rgba(230,230,230,0.7);");
		return borderPane;
	}
	
	public static void startValueSetAnimation(final Pane parent) {
		final javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle();
		Insets margin = BorderPane.getMargin(parent);
		if (margin==null){
			margin= new Insets(0);
		}
		rectangle.widthProperty().bind(parent.widthProperty().subtract(margin.getLeft()+margin.getRight()));
		rectangle.heightProperty().bind(parent.heightProperty().subtract(margin.getTop()+margin.getBottom()));
		rectangle.setFill(Color.rgb(0, 150, 201));
		parent.getChildren().add(rectangle);
		
        BoxBlur bb = new BoxBlur();
        bb.setWidth(5);
        bb.setHeight(5);
        bb.setIterations(3);
        rectangle.setEffect(bb);
        
		FadeTransition ft = new FadeTransition(Duration.millis(250), rectangle);
		ft.setFromValue(0.2);
		ft.setToValue(0.8);
		ft.setCycleCount(2);
		ft.setAutoReverse(true);
		ft.play();
		ft.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parent.getChildren().remove(rectangle);
			}
		});
	}
	
	
	public static void executeWithProgressDialogInBackground(final Runnable runnable, final StackPane target, final String text){
		Thread th = new Thread(){
			@Override
			public void run() {
				final Node progressIndicator = createProgressIndicator();
				final Label label = new Label(text);
				try {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							target.getChildren().add(progressIndicator);
							label.setWrapText(true);
							target.getChildren().add(label);
						}
					});
					runnable.run();
				} finally {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							target.getChildren().remove(progressIndicator);
							target.getChildren().remove(label);
						}
					});	
				}
			}
		};
		th.setDaemon(true);
		th.start();
	}
	
	
	public static void setupXAxis(NumberAxis numberAxis, ObservableList<XYChart.Series<Number, Number>> seriesList){
		long min=Long.MAX_VALUE;
		long max=0;

		for (XYChart.Series<Number, ?> series: seriesList){
			for (Data<Number, ?> data: series.getData()){
				min = Math.min(data.getXValue().longValue(),min);
				max = Math.max(data.getXValue().longValue(),max);
			}
		}
		setupXAxis(numberAxis, min, max );
	}
	
	public static void setupXAxis(NumberAxis numberAxis, long min, long max ){
		numberAxis.setAutoRanging(false);
		numberAxis.setTickUnit((max-min)/20);
		numberAxis.setLowerBound(min);
		numberAxis.setUpperBound(max);
		numberAxis.setTickLabelFormatter(new StringConverter<Number>() {
			private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss,SSS");
			@Override
			public String toString(Number object) {
				return format.format(new Date(object.longValue()));
			}
			
			@Override
			public Number fromString(String string) {
				return null;
			}
		});
	}

}
