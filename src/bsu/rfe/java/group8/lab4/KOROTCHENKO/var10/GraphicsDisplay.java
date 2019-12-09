package bsu.rfe.java.group8.lab4.KOROTCHENKO.var10;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {

    private Double[][] graphicsData;
    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean showDelta = true;
    private boolean scaleMode = false;
    private boolean isZoomed = false;
    private Double[] originalPoint = new Double[2];
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();
    private int selectedMarker = -1;
    private static DecimalFormat formatter=(DecimalFormat) NumberFormat.getInstance();

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double minXo;
    private double maxXo;
    private double minYo;
    private double maxYo;

    private double scale;
    private double scaleX;
    private double scaleY;

    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke deltaStroke;
    private BasicStroke selectionStroke;

    private Font axisFont;
    private Font labelsFont;


    public GraphicsDisplay() {
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[]{40, 10, 10, 10, 20, 10, 10, 10, 40, 20}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        deltaStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        selectionStroke = new BasicStroke(1.0F, 0, 0, 10.0F, new float[] { 10, 10 }, 0.0F);
        axisFont = new Font("Serif", Font.BOLD, 36);
        labelsFont = new java.awt.Font("Serif",0,10);
        addMouseMotionListener(new MouseMotionHandler());
        addMouseListener(new MouseHandler());
    }

    public void showGraphics(Double[][] graphicsData) {
        this.graphicsData = graphicsData;

        this.minXo = graphicsData[0][0];
        this.maxXo = graphicsData[graphicsData.length -1][0];
        this.minYo = graphicsData[0][1];
        this.maxYo = this.minYo;

        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < this.minYo) {
                this.minYo = graphicsData[i][1];
            }
            if (graphicsData[i][1] > this.maxYo) {
                this.maxYo = graphicsData[i][1];
            }
        }

        zoomToRegion(minXo, maxYo, maxXo, minYo);

        repaint();
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowDelta(boolean showDelta){
        this.showDelta = showDelta;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if (graphicsData==null || graphicsData.length==0) return;

        if (scaleMode == false){
            scaleX = this.getSize().getWidth() / (maxX - minX);
            scaleY = this.getSize().getHeight() / (maxY - minY);

            scale = Math.min(scaleX, scaleY);

            if (scale==scaleX) {
                double yIncrement = (getSize().getHeight()/scale - (maxY - minY))/2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale==scaleY) {
                double xIncrement = (getSize().getWidth()/scale - (maxX - minX))/2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        paintLabels(canvas);
        if (showMarkers) paintMarkers(canvas);
        if (showDelta) paintDelta(canvas);
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
        paintSelection(canvas);
    }

    private void paintSelection(Graphics2D canvas) {
        if (!scaleMode) return;
        canvas.setStroke(selectionStroke);
        canvas.setColor(Color.BLACK);
        canvas.draw(selectionRect);
    }

    protected void paintGraphics(Graphics2D canvas){
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.BLUE);
        GeneralPath graphics = new GeneralPath();
        int j = 0;
        for (int i=0; i<graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if ((graphicsData[i][0] >= minX) && (graphicsData[i][1] <= maxY) &&
                    (graphicsData[i][0] <= maxX) && (graphicsData[i][1] >= minY)) {
                if (j > 0) {
                    graphics.lineTo(point.getX(), point.getY());
                } else {
                    graphics.moveTo(point.getX(), point.getY());
                }
                j++;
            }
        }
        canvas.draw(graphics);
    }

    protected void paintMarkers(Graphics2D canvas) {

        for (Double[] point : graphicsData){
            boolean check = false;
            Double value = point[1];
            value = Math.abs(value - Math.floor(value));
            if (value <= 0.1 || value >= 0.9){
                check = true;
            }

            if (check){
                canvas.setColor(Color.GREEN);
                canvas.setPaint(Color.GREEN);
            }
            else{
                canvas.setColor(Color.RED);
                canvas.setPaint(Color.RED);
            }

            canvas.setStroke(markerStroke);
            Point2D.Double center = xyToPoint(point[0], point[1]);
            canvas.draw(new Line2D.Double(shiftPoint(center, -11, 11), shiftPoint(center, 11, 11)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -11, -11), shiftPoint(center, 11, -11)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 11, 11), shiftPoint(center, 11, -11)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -11, -11), shiftPoint(center, -11, 11)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 11, 11), shiftPoint(center, -11, -11)));
            canvas.draw(new Line2D.Double(shiftPoint(center, -11, 11), shiftPoint(center, 11, -11)));

        }
    }

    protected void paintDelta(Graphics2D canvas){
        FontRenderContext context = canvas.getFontRenderContext();
        Double step_x = (maxX - minX)/10;
        Double step_y = (maxY - minY)/10;
        int i = 0;
        canvas.setStroke(deltaStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        Double step = Math.min(step_x, step_y)*2.0/3.0;
        for (Double temp = minX; temp < maxX; temp += step_x){
                canvas.draw(new Line2D.Double(xyToPoint(temp, step/2 - 1), xyToPoint(temp, -step/2 + 1)));

            Point2D.Double coordLabelPos = xyToPoint(temp, step/2);
            Rectangle2D coordbounds = axisFont.getStringBounds("0", context);
            canvas.drawString("x = " + temp, (float)coordLabelPos.getX() - 35, (float)(coordLabelPos.getY() - coordbounds.getY()));

            i++;
        }
        i = 0;
        for (Double temp = minY; temp < maxY; temp += step_y){
                canvas.draw(new Line2D.Double(xyToPoint(step/2 - 1, temp), xyToPoint(-step/2 + 1, temp)));

            Point2D.Double coordLabelPos = xyToPoint(step/2, temp );
            Rectangle2D coordbounds = axisFont.getStringBounds("0", context);
            canvas.drawString("y = " + temp, (float)coordLabelPos.getX() - 15, (float)(coordLabelPos.getY() - coordbounds.getY()) - 30);

            i++;
        }
    }


    protected void paintLabels(Graphics2D canvas){
        canvas.setColor(Color.BLACK);
        canvas.setFont(this.labelsFont);
        FontRenderContext context=canvas.getFontRenderContext();
        if (selectedMarker >= 0)
        {
            Point2D.Double point = xyToPoint(graphicsData[selectedMarker][0], graphicsData[selectedMarker][1]);
            String label = "X=" + formatter.format(graphicsData[selectedMarker][0]) + ", Y=" + formatter.format(graphicsData[selectedMarker][1]);
            Rectangle2D bounds = labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLACK);
            canvas.drawString(label, (float)(point.getX() + 5.0D), (float)(point.getY() - bounds.getHeight()));
        }
    }

    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();




        if (minX <= 0.0 && maxX >= 0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX()+5, arrow.getCurrentPoint().getY()+20);
            arrow.lineTo(arrow.getCurrentPoint().getX()-10, arrow.getCurrentPoint().getY());

            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);




            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }

        if (minY<=0.0 && maxY>=0.0) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));
            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() + bounds.getY()));
        }

    }


    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX*scale, deltaY*scale);
    }

    protected Double[] translatePointToXY(int x, int y)
    {
        return new Double[] { minX + x / this.scaleX, maxY - y / this.scaleY };
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2){
        minX = x1;
        maxY = y1;
        maxX = x2;
        minY = y2;
        this.repaint();
    }

   protected int findSelectedPoint(int x, int y)
    {
        if (graphicsData == null) return -1;
        int pos = 0;
        for (Double[] point : graphicsData) {
            Point2D.Double screenPoint = xyToPoint(point[0].doubleValue(), point[1].doubleValue());
            double distance = (screenPoint.getX() - x) * (screenPoint.getX() - x) + (screenPoint.getY() - y) * (screenPoint.getY() - y);
            if (distance < 100) return pos;
            pos++;
        }	    return -1;
    }

    public class MouseHandler extends MouseAdapter{
        public MouseHandler(){};

        public void mouseClicked(MouseEvent ev) {
            if (ev.getButton() == 3) {
                isZoomed = false;
                zoomToRegion(minXo, maxYo, maxXo, minYo);
                repaint();
            }
        }

        public void mousePressed(MouseEvent ev){
            if (ev.getButton() != 1 || isZoomed) return;
            scaleMode = true;
            setCursor(Cursor.getPredefinedCursor(5));
            selectionRect.setFrame(ev.getX(), ev.getY(), 1.0D, 1.0D);
            originalPoint = translatePointToXY(ev.getX(), ev.getY());
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() != 1 || isZoomed) return;
            setCursor(Cursor.getPredefinedCursor(0));
            isZoomed = true;
            scaleMode = false;
            Double[] finalPoint = new Double[2];
            finalPoint = translatePointToXY(ev.getX(), ev.getY());
            zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
            repaint();
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {

        public void mouseDragged(MouseEvent ev) {
            double width = ev.getX() - selectionRect.getX();
            if (width < 5.0D) {
                width = 5.0D;
            }
            double height = ev.getY() - selectionRect.getY();
            if (height < 5.0D) {
                height = 5.0D;
            }
            selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
            selectedMarker = findSelectedPoint(ev.getX(), ev.getY());
            repaint();
        }
    }
}
