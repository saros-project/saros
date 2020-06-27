package saros.negotiation.additional_resource_data;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.resources.IContainer;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.negotiation.AdditionalResourceDataFactory;

/**
 * Eclipse class providing possible representations of the shared reference point by using the
 * project name and project-relative path of the reference point delegate.
 */
public class EclipsePossibleRepresentationProvider extends AbstractPossibleRepresentationProvider {

  public EclipsePossibleRepresentationProvider(
      AdditionalResourceDataFactory additionalResourceDataFactory) {

    super(additionalResourceDataFactory);
  }

  @Override
  protected List<Pair<String, String>> computePossibleRepresentations(
      IReferencePoint referencePoint) {
    IContainer referencePointDelegate = ResourceConverter.getDelegate(referencePoint);

    String projectName = referencePointDelegate.getProject().getName();
    String projectRelativePath = referencePointDelegate.getProjectRelativePath().toPortableString();

    return Collections.singletonList(new ImmutablePair<>(projectName, projectRelativePath));
  }
}
