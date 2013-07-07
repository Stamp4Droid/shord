package stamp.util;

import shord.project.ClassicProject;
import shord.project.analyses.ProgramRel;

/**
 * A map from names to relations, which is filled lazily as new relations are
 * requested.
 *
 * Any changes made to the mapped relations are only stored to disk when the
 * map is {@link #clear() cleared}.
 */
public class RelMap extends LazyMap<String,ProgramRel> {
	private final boolean createNew;

	/**
	 * @param createNew whether to instantiate new relations in case they don't
	 *        already exist
	 */
	public RelMap(boolean createNew) {
		this.createNew = createNew;
	}

	@Override
	public ProgramRel lazyFill(String relName) {
		// TODO: Can this fail for non-existing relations? Need to configure
		// targets / skip Project infrastructure?
		ProgramRel rel =  (ProgramRel) ClassicProject.g().getTrgt(relName);
		if (!rel.isOpen()) {
			if (createNew && !ClassicProject.g().isTrgtDone(relName)) {
				// Only initialize a new relation if we haven't yet added any
				// tuples to it on a previous step. Otherwise, first load the
				// existing tuples before proceeding.
				rel.zero();
			} else {
				rel.load();
			}
		}
		return rel;
	}

	/**
	 * Empty the map, storing all changes to the mapped ProgramRels to disk and
	 * removing them from memory.
	 */
	@Override
	public void clear() {
		for (ProgramRel rel : values()) {
			rel.save();
		}
		super.clear();
	}
}
