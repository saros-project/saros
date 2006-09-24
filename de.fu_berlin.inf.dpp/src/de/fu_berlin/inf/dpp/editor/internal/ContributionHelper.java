package de.fu_berlin.inf.dpp.editor.internal;

import java.util.Iterator;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

import de.fu_berlin.inf.dpp.editor.annotations.ContributionAnnotation;

public class ContributionHelper implements IDocumentListener {
    
    public static void insertContribution(IAnnotationModel model, int offset, 
        int length) {
        
        if (length > 0) {
            Annotation annotation = new ContributionAnnotation("");
            
            Position position = new Position(offset, length);
            model.addAnnotation(annotation, position);
        }
    }
    
    public static void insertNeutral(IAnnotationModel model, int offset, 
        int length) {
        
        for (Iterator it = model.getAnnotationIterator(); it.hasNext();) {
            Annotation annotation = (Annotation)it.next();
            Position pos = model.getPosition(annotation);
            
            if (offset >= pos.offset && offset < pos.offset+pos.length) {
                Position pos1 = new Position(pos.offset, offset-pos.offset);
                Position pos2 = new Position(offset+length, pos.length - (offset-pos.offset));
                
                model.removeAnnotation(annotation);
                model.addAnnotation(new ContributionAnnotation(), pos1);
                model.addAnnotation(new ContributionAnnotation(), pos2);
            }
        }
    }
    
    public void documentAboutToBeChanged(DocumentEvent event) {
        // TODO Auto-generated method stub
    }

    public void documentChanged(DocumentEvent event) {
        // TODO Auto-generated method stub
    }
}
