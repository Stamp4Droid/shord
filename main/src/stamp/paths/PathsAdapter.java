package stamp.paths;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;

import shord.analyses.DomI;
import shord.analyses.DomF;
import shord.analyses.DomV;
import shord.analyses.DomU;
import stamp.analyses.DomL;
import stamp.paths.raw.BaseStep;
import stamp.paths.raw.NonTerminalStep;
import stamp.paths.raw.ObjectFactory;
import stamp.paths.raw.PathsList;
import stamp.paths.raw.TemporaryStep;
import stamp.paths.raw.TerminalStep;
import stamp.util.DomMap;
import chord.util.tuple.object.Pair;
import stamp.util.PropertyHelper;
import stamp.util.StringHelper;

public class PathsAdapter {
	private final DomMap doms = new DomMap();
	private final JAXBContext jc;
	private final Schema schema;

	public PathsAdapter(String schemaFile) throws TranslationException {
		try {
			// Feed JAXB the package where schema-generated classes live.
			jc = JAXBContext.newInstance("stamp.paths.raw");
			// Specify a schema file, to validate the paths XML file against.
			// This should be the same schema that generated the classes in the
			// above package.
			SchemaFactory sf =
				SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
			schema = sf.newSchema(new File(schemaFile));
		} catch (JAXBException exc) {
			throw new TranslationException(exc);
		} catch (SAXException exc) {
			throw new TranslationException(exc);
		}
	}

	public List<Path> getFlatPaths(String rawPathsFile)
		throws TranslationException {
		try {
			PathsList rawPaths = readRawPaths(rawPathsFile);
			flattenRawPaths(rawPaths, true);
			return convertPaths(rawPaths);
		} catch (JAXBException exc) {
			throw new TranslationException(exc);
		}
	}

	public void normalizeRawPaths(String rawPathsFile,
								  String normalPathsFile)
		throws TranslationException  {
		try {
			PathsList rawPaths = readRawPaths(rawPathsFile);
			flattenRawPaths(rawPaths, false);
			translateNodeNames(rawPaths, true);
			writeRawPaths(rawPaths, normalPathsFile);
		} catch (JAXBException exc) {
			throw new TranslationException(exc);
		} catch (FileNotFoundException exc) {
			throw new TranslationException(exc);
		}
	}

	public static class TranslationException extends Exception {
		public TranslationException(String msg) {
			super(msg);
		}

		public TranslationException(Throwable cause) {
			super(cause);
		}
	}

	private PathsList readRawPaths(String inFile) throws JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		u.setSchema(schema);
		// Fail at the first validation error.
		u.setEventHandler(new DefaultValidationEventHandler());
		JAXBElement<PathsList> root =
			(JAXBElement<PathsList>) u.unmarshal(new File(inFile));
		return root.getValue();
	}

	private void writeRawPaths(PathsList rawPaths, String outFile)
		throws FileNotFoundException, JAXBException {
        Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// TODO: We don't validate the rawPaths tree, because the calling code
		// alters the tree in such a way that it no longer adheres to some of
		// the non-structural constraints in the schema.
        //m.setSchema(schema);
		//m.setEventHandler(new DefaultValidationEventHandler());
        m.marshal(new ObjectFactory().createPaths(rawPaths),
				  new FileOutputStream(outFile));
	}

	// Expects rawPaths to be fully flat.
	private List<Path> convertPaths(PathsList rawPaths)
		throws TranslationException {
		List<Path> paths = new ArrayList<Path>();

		for (stamp.paths.raw.Edge rawEdge : rawPaths.getEdges()) {
			Point start = rawNodeToPoint(rawEdge.getFrom());
			Point end = rawNodeToPoint(rawEdge.getTo());
			for (stamp.paths.raw.Path rawPath : rawEdge.getPaths()) {

				List<Step> steps = new ArrayList<Step>();
				for (JAXBElement<? extends BaseStep> e :
						 rawPath.getTopStep().getSubSteps()) {
					BaseStep ss = e.getValue();
					// The sub-steps of the top step should be completely flat,
					// ergo this should be a terminal step.
					steps.add(translateTerminalStep((TerminalStep) ss));
				}

				paths.add(new Path(start, end, steps));
			}
		}

		doms.clear();
		return paths;
	}

	// TODO: Ideally, we would have a constructor on the cooked Step class that
	// takes a compatible raw Step, but this would require the cooked Step
	// class to be able to translate node names.

	private Step translateTerminalStep(TerminalStep s)
		throws TranslationException {
		boolean reverse = s.isReverse();
		String tgtNode = reverse ? s.getFrom() : s.getTo();
		Point tgtPoint = rawNodeToPoint(tgtNode);

		if (s instanceof stamp.paths.raw.IntraProceduralStep) {
			return new IntraProceduralStep(reverse, tgtPoint);
		} else if (s instanceof stamp.paths.raw.LoadStep) {
			int f = ((stamp.paths.raw.LoadStep) s).getIndex().intValue();
			return new LoadStep(reverse, tgtPoint,
								((DomF) doms.get("F")).get(f));
		} else if (s instanceof stamp.paths.raw.StoreStep) {
			int f = ((stamp.paths.raw.StoreStep) s).getIndex().intValue();
			return new StoreStep(reverse, tgtPoint,
								 ((DomF) doms.get("F")).get(f));
		} else if (s instanceof stamp.paths.raw.CallStep) {
			int i = ((stamp.paths.raw.CallStep) s).getIndex().intValue();
			return new CallStep(reverse, tgtPoint,
								((DomI) doms.get("I")).get(i));
		} else if (s instanceof stamp.paths.raw.ReturnStep) {
			int i = ((stamp.paths.raw.ReturnStep) s).getIndex().intValue();
			return new ReturnStep(reverse, tgtPoint,
								  ((DomI) doms.get("I")).get(i));
		} else if (s instanceof stamp.paths.raw.StackCrossingStep) {
			return new StackCrossingStep(reverse, tgtPoint);
		}

		// Shouldn't reach here.
		assert(false);
		return null;
	}

	// TODO: The following methods modify raw PathsLists in-place, and
	// conceptually belong in the classes of stamp.paths.raw, but we'd need to
	// edit the JAXB binding scheme to add them.

	private void flattenRawPaths(PathsList rawPaths, boolean fully) {
		for (stamp.paths.raw.Edge rawEdge : rawPaths.getEdges()) {
			for (stamp.paths.raw.Path rawPath : rawEdge.getPaths()) {
				flattenRawSubSteps(rawPath.getTopStep(), fully);
			}
		}
	}

	private void flattenRawSubSteps(NonTerminalStep step, boolean fully) {
		List<JAXBElement<? extends BaseStep>> flatSubSteps =
			new ArrayList<JAXBElement<? extends BaseStep>>();

		for (JAXBElement<? extends BaseStep> e : step.getSubSteps()) {
			BaseStep ss = e.getValue();
			if (ss instanceof NonTerminalStep) {
				NonTerminalStep ntss = (NonTerminalStep) ss;
				flattenRawSubSteps(ntss, fully);
				if (fully || ntss instanceof TemporaryStep) {
					// We skip this step, and instead record its sub-steps
					// directly.
					// TODO: Could also skip some non-terminals that don't
					// offer much information, e.g. in the case of transitive
					// rules.
					if (ntss.isReverse()) {
						// If the step to skip was traversed in reverse, we
						// need to reverse the order in which we record its
						// sub-steps.
						reverseSubSteps(ntss);
					}
					flatSubSteps.addAll(ntss.getSubSteps());
					continue;
				}
			}
			// Terminal steps and steps corresponding to non-intermediate
			// symbols are normally retained.
			flatSubSteps.add(e);
		}

		// Replace the original sub-steps list with the flattened one.
		// TODO: The JAXB-generated class for raw Steps doesn't allow us to
		// simply swap out its sub-steps list for another. We instead have to
		// make a copy of the flattened list.
		step.getSubSteps().clear();
		step.getSubSteps().addAll(flatSubSteps);
	}

	private void reverseSubSteps(NonTerminalStep rawStep) {
		// Reverse the order that we traverse the sub-steps.
		Collections.reverse(rawStep.getSubSteps());
		// Also switch the 'reverse' modifier on each of the sub-steps.
		for (JAXBElement<? extends BaseStep> e : rawStep.getSubSteps()) {
			BaseStep ss = e.getValue();
			ss.setReverse(!ss.isReverse());
		}
	}

	private void translateNodeNames(PathsList rawPaths, boolean useShortNames)
		throws TranslationException {
		for (stamp.paths.raw.Edge rawEdge : rawPaths.getEdges()) {
			Point from = rawNodeToPoint(rawEdge.getFrom());
			rawEdge.setFrom(useShortNames ? from.toShortString()
							: from.toString());
			Point to = rawNodeToPoint(rawEdge.getTo());
			rawEdge.setTo(useShortNames ? to.toShortString() : to.toString());
			// TODO: The context is not added to the variables points.

			for (stamp.paths.raw.Path rawPath : rawEdge.getPaths()) {
				translateNodeNames(rawPath.getTopStep(), useShortNames);
			}
		}

		doms.clear();
	}

	private void translateNodeNames(BaseStep rawStep, boolean useShortNames)
		throws TranslationException {
		Point from = rawNodeToPoint(rawStep.getFrom());
		rawStep.setFrom(useShortNames ? from.toShortString()
						: from.toString());
		Point to = rawNodeToPoint(rawStep.getTo());
		rawStep.setTo(useShortNames ? to.toShortString() : to.toString());
		// TODO: Indices are left untranslated.
		// TODO: The context is not added to the variables points.

		if (rawStep instanceof NonTerminalStep) {
			NonTerminalStep ntStep = (NonTerminalStep) rawStep;
			for (JAXBElement<? extends BaseStep> e : ntStep.getSubSteps()) {
				translateNodeNames(e.getValue(), useShortNames);
			}
		}
	}

	// TODO: In the following, we have hardcoded information regarding the node
	// naming scheme.

	private Point rawNodeToPoint(String rawNode) throws TranslationException {
		char tag = rawNode.charAt(0);
		switch (tag) {
		case 'v':
			int v = getSingleDomIndex(rawNode);
			return new VarPoint(((DomV) doms.get("V")).get(v));
		case 'u':
			int u = getSingleDomIndex(rawNode);
			return new VarPoint(((DomU) doms.get("U")).get(u));
		case 'o':
			int o = getSingleDomIndex(rawNode);
			return new ObjPoint(((DomI) doms.get("I")).get(o));
		case 'f':
			int f = getSingleDomIndex(rawNode);
			return new StatFldPoint(((DomF) doms.get("F")).get(f));
		case 'l':
			int l = getSingleDomIndex(rawNode);
			return new LabelPoint(((DomL) doms.get("L")).get(l));
		default:
			throw new TranslationException("Invalid node name: " + rawNode);
		}
	}

	private static int getSingleDomIndex(String rawNode) {
		return Integer.parseInt(rawNode.substring(1));
	}

	private static Pair<Integer,Integer> getTwoDomIndices(String rawNode)
		throws TranslationException {
		List<String> indexStrs = StringHelper.split(rawNode.substring(1), "_");
		if (indexStrs.size() != 2) {
			throw new TranslationException("Invalid node name: " + rawNode);
		}
		return new Pair<Integer,Integer>(new Integer(indexStrs.get(0)),
										 new Integer(indexStrs.get(1)));
	}
}
