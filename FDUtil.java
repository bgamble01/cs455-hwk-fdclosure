import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

/**
 * This utility class is not meant to be instantitated, and just provides some
 * useful methods on FD sets.
 * 
 * @author <<Ben Gamble>>
 * @version <<Oct 29>>
 */
public final class FDUtil {

  /**
   * Resolves all trivial FDs in the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return a set of trivial FDs with respect to the given FDSet
   */
  public static FDSet trivial(final FDSet fdset) {
     FDSet trivs = new FDSet();
     FDSet copied = new FDSet(fdset);
     for (FD individual: copied) {
        Set<Set<String>> power= powerSet(individual.getLeft());
        for (Set<String> gen:power) {
           if(!gen.isEmpty()) {
              trivs.add(new FD(individual.getLeft(),gen));
           }
        }
     }
     return trivs;

    
  }

  /**
   * Augments every FD in the given set of FDs with the given attributes
   * 
   * @param fdset FD Set (Immutable)
   * @param attrs a set of attributes with which to augment FDs (Immutable)
   * @return a set of augmented FDs
   */
  public static FDSet augment(final FDSet fdset, final Set<String> attrs) {

      FDSet augs = new FDSet();
      FDSet copied = new FDSet(fdset);
      
      for ( FD individual : copied) {
            FD indiv_copy = new FD(individual);
            indiv_copy.addToLeft(attrs);
            indiv_copy.addToRight(attrs);
         
            augs.add(indiv_copy);
         

      }
    return augs;

  }

  /**
   * Exhaustively resolves transitive FDs with respect to the given set of FDs
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitive(final FDSet fdset) {
     FDSet copied = new FDSet(fdset);
     
     FDSet found= transitivehelp(copied);
     
     found.getSet().removeAll(copied.getSet());
     
     // set subtraction to remove original FDs
     
     return found;

  }
  
  
  /**
   * Helper method that includes the original FDset
   * 
   * @param fdset (Immutable) FD Set
   * @return all transitive FDs with respect to the input FD set
   */
  public static FDSet transitivehelp(final FDSet fdset) {
  FDSet copied = new FDSet(fdset);
  FDSet adds = new FDSet();
  
  for (FD fd1 : copied) {
     for (FD fd2 : copied) {
        if (!fd1.equals(fd2)) {
           if((fd1.getRight()).equals(fd2.getLeft())) {
              adds.add(new FD(fd1.getLeft(),fd2.getRight()));
           }
        }
     }
  }
  
  if(copied.getSet().containsAll(adds.getSet())) {
     return fdset;
  }

  
  copied.addAll(adds);
  
  return transitivehelp(copied);
}

  /*
   * Generates the closure of the given FD Set
   * 
   * @param fdset (Immutable) FD Set
   * @return the closure of the input FD Set
   */
   public static FDSet fdSetClosure(final FDSet fdset) {

      FDSet copied = new FDSet(fdset);
      FDSet temporary = new FDSet(fdset);
      boolean check = true;

      // generate the powerset of all original attributes that will be used
      // multiple times below
      Set<String> attrs = new HashSet<>();
      for (FD temp : fdset) {
         attrs.addAll(temp.getLeft());
         attrs.addAll(temp.getRight());
      }
      Set<Set<String>> power = powerSet(attrs);

      int currlen;
      while (check == true) {
         currlen = copied.size();

         for (Set<String> gen : power) {
            if (!gen.isEmpty()) {
               temporary = augment(copied, gen);
               copied.addAll(temporary);
            }
         }
         temporary = trivial(copied);
         copied.addAll(temporary);

         temporary = transitive(copied);
         copied.addAll(temporary);

         if (copied.size() == currlen) {
            check = false;
         }
      }
      return copied;
   }
   
   
  /**
   * Generates the power set of the given set (that is, all subsets of
   * the given set of elements)
   * 
   * @param set Any set of elements (Immutable)
   * @return the power set of the input set
   */
  @SuppressWarnings("unchecked")
  public static <E> Set<Set<E>> powerSet(final Set<E> set) {

    // base case: power set of the empty set is the set containing the empty set
    if (set.size() == 0) {
      Set<Set<E>> basePset = new HashSet<>();
      basePset.add(new HashSet<>());
      return basePset;
    }

    // remove the first element from the current set
    E[] attrs = (E[]) set.toArray();
    set.remove(attrs[0]);

    // recurse and obtain the power set of the reduced set of elements
    Set<Set<E>> currentPset = FDUtil.powerSet(set);

    // restore the element from input set
    set.add(attrs[0]);

    // iterate through all elements of current power set and union with first
    // element
    Set<Set<E>> otherPset = new HashSet<>();
    for (Set<E> attrSet : currentPset) {
      Set<E> otherAttrSet = new HashSet<>(attrSet);
      otherAttrSet.add(attrs[0]);
      otherPset.add(otherAttrSet);
    }
    currentPset.addAll(otherPset);
    return currentPset;
  }
}
