package stamp.paths;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import org.xml.sax.SAXException;

import shord.analyses.Ctxt;
import shord.analyses.DomC;
import shord.analyses.DomF;
import shord.analyses.DomV;
import shord.analyses.DomU;
import stamp.analyses.DomCL;
import stamp.util.DomMap;
import chord.util.tuple.object.Pair;
import stamp.util.PropertyHelper;
import stamp.util.StringHelper;

public class PathsAdapter {
	private final DomMap doms = new DomMap();

	private PathsAdapter() {}

	public static List<Path> getPaths() {
		String schemaFile = PropertyHelper.getProperty("stamp.paths.schema");
		String pathsFile = PropertyHelper.getProperty("stamp.paths.file");
		return new PathsAdapter().getPaths(schemaFile, pathsFile);
	}

	private List<Path> getPaths(String schemaFile, String pathsFile) {
		stamp.paths.raw.Paths rawPaths;
		try {
			rawPaths = getRawPaths(schemaFile, pathsFile);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		List<Path> paths = new ArrayList<Path>();

		for (stamp.paths.raw.Edge rawEdge : rawPaths.getEdge()) {
			Point start = rawNodeToPoint(rawEdge.getFrom());
			Point end = rawNodeToPoint(rawEdge.getTo());
			for (stamp.paths.raw.Path rawPath : rawEdge.getPath()) {
				List<Step> steps = new ArrayList<Step>();
				flattenSteps(rawPath.getStep(), steps, false);
				paths.add(new Path(start, end, steps));
			}
		}

		doms.clear();
		return paths;
	}

	private stamp.paths.raw.Paths getRawPaths(String schemaFile,
											  String pathsFile)
		throws SAXException, UnmarshalException, JAXBException {
		// Feed JAXB the package where schema-generated classes live.
		JAXBContext jc = JAXBContext.newInstance("stamp.paths.raw");
		Unmarshaller u = jc.createUnmarshaller();
		// Specify a schema file, to validate the paths XML file against. This
		// should be the same schema that generated the classes in the above
		// package.
		SchemaFactory sf = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(new File(schemaFile));
		u.setSchema(schema);
		// Fail at the first validation error.
		u.setEventHandler(new DefaultValidationEventHandler());
		return (stamp.paths.raw.Paths) u.unmarshal(new File(pathsFile));
	}

	private void flattenSteps(stamp.paths.raw.Step rawStep,
							  List<Step> flatSteps, boolean parentReverse) {
		boolean inReverse = rawStep.isReverse() ^ parentReverse;
		List<stamp.paths.raw.Step> rawSubSteps = rawStep.getStep();

		if (rawSubSteps.isEmpty()) {
			// This covers both terminal steps and empty productions.
			// TODO: Also require that the symbol be terminal.
			// This information is already being retrieved in FactsDumper, we
			// should just cache it.
			String tgtNode = inReverse ? rawStep.getFrom() : rawStep.getTo();
			Point tgtPoint = rawNodeToPoint(tgtNode);
			flatSteps.add(new Step(rawStep.getSymbol(), inReverse, tgtPoint));
			return;
		}

		// If this Edge was traversed in reverse, we also need to reverse the
		// order in which we visit its sub-Edges.
		// TODO: We could simply use a reverse iterator instead of reversing
		// the list, but these step lists are tiny anyway.
		if (inReverse) {
			Collections.reverse(rawSubSteps);
		}
		for (stamp.paths.raw.Step s : rawSubSteps) {
			flattenSteps(s, flatSteps, inReverse);
		}
	}

	private Point rawNodeToPoint(String rawNode) {
		char tag = rawNode.charAt(0);
		switch (tag) {
		case 'v':
			Pair<Integer,Integer> vc = getTwoDomIndices(rawNode);
			int v = vc.val0.intValue();
			int c_v = vc.val1.intValue();
			return new CtxtVarPoint(((DomC) doms.get("C")).get(c_v),
									((DomV) doms.get("V")).get(v));
		case 'u':
			Pair<Integer,Integer> uc = getTwoDomIndices(rawNode);
			int u = uc.val0.intValue();
			int c_u = uc.val1.intValue();
			return new CtxtVarPoint(((DomC) doms.get("C")).get(c_u),
									((DomU) doms.get("U")).get(u));
		case 'o':
			int o = getSingleDomIndex(rawNode);
			return new CtxtObjPoint(((DomC) doms.get("C")).get(o));
		case 'f':
			int f = getSingleDomIndex(rawNode);
			return new StatFldPoint(((DomF) doms.get("F")).get(f));
		case 'l':
			int cl = getSingleDomIndex(rawNode);
			Pair<String,Ctxt> ctxtLabel = ((DomCL) doms.get("CL")).get(cl);
			return new CtxtLabelPoint(ctxtLabel.val1, ctxtLabel.val0);
		default:
			throw new RuntimeException("Invalid node name: " + rawNode);
		}
	}

	private static int getSingleDomIndex(String rawNode) {
		return Integer.parseInt(rawNode.substring(1));
	}

	private static Pair<Integer,Integer> getTwoDomIndices(String rawNode) {
		List<String> indexStrs = StringHelper.split(rawNode.substring(1), "_");
		if (indexStrs.size() != 2) {
			throw new RuntimeException("Invalid node name: " + rawNode);
		}
		return new Pair<Integer,Integer>(new Integer(indexStrs.get(0)),
										 new Integer(indexStrs.get(1)));
	}
}
