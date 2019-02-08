package de.fu_berlin.inf.dpp.ui.model

/**
 * Represents a project. This is an abstraction of the different concepts of
 * what an "project" is in different IDEs. A ProjectTree has {@link Node} which
 * represents the root.
 * 
 * This class is used to form an JSON equivalent for the HTML UI.
 */
public class ProjectTree
	
	/**
     * @param root
     *            the node representing the project itself
     */
	(private val root:Node) {
	init{
		if(root.type != NodeType.PROJECT){
			throw IllegalArgumentException("root node needs to be a PROJECT")
		}
	}
	
	public fun getProjectName() : String{
		return root.label
	}
	
	public fun getRoot():Node{
		return root
	}
	
	/**
     * This represents a project or a resource (folder, file) inside a project.
     * Several of these nodes make up a tree structure.
     */
	public data class Node(val members:MutableList<Node>,val label : String, val type : NodeType, val isSelectedForSharing : Boolean ){
		
		private val hash = calculateHash() 
		
		/**
         * Create a {@link Node} representing a file.
         * 
         * @param label
         *            the file name
         * @param isSelectedForSharing
         *            determines whether this node is (pre)selected to be shared
         *            in a session.
         * @return a node representing a file (has no members)
         */
		
		companion object{
			@JvmStatic
			fun fileNode(label : String, isSelectedForSharing : Boolean):Node{
				return Node(ArrayList<Node>(), label, NodeType.FILE, isSelectedForSharing)
			}
		}
		
        override public fun toString():String {
            return label + " (" + type + ")";
        }

        /**
         * Two {@link Node}s are considered equals if they have the same
         * {@link #type}, {@link #label}, and {@link #members} match. The field
         * {@link #isSelectedForSharing} is not considered.
         */
         override fun equals(other : Any?):Boolean{
            if (!(other is Node)) {
                return false;
            }

            if (!this.label.equals(other.label))
                return false;

            if (this.type != other.type)
                return false;

            if (!this.members.containsAll(other.members))
                return false;

            if (!other.members.containsAll(this.members))
                return false;

            return true;
        }
		
		/**
         * Two {@link Node}s get the same hash if their {@link #type},
         * {@link #label}, and the hash of their {@link #members} match. The
         * field {@link #isSelectedForSharing} is not considered.
         */
		override fun hashCode():Int{
			return hash
		}
		
		private fun calculateHash() : Int {
            var prime = 31;
            var result = prime + label.hashCode();
            result = prime * result + type.hashCode();

            for (member in members) {
                result = prime * result + member.hashCode();
            }

            return result;
		} 	
	}
	/**
         * Type of resource a {@link Node} can represent.
         * <p>
         * A {@link Node} can be a parent for other {@link Node}s (i.e. a
         * {@link #PROJECT} or a {@link #FOLDER}, which may have
         * {@link Node#getMembers() members}), or just be a {@link #FILE}.
         */
		public enum class NodeType{
			PROJECT, FOLDER, FILE
		}
}