package org.aksw.limes.core.ml.algorithm.eagle.genes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.aksw.limes.core.datastrutures.PairSimilar;
import org.aksw.limes.core.measures.measure.MeasureFactory;
import org.aksw.limes.core.ml.algorithm.eagle.core.LinkSpecGeneticLearnerConfig;
import org.aksw.limes.core.ml.algorithm.eagle.core.ExpressionProblem.ResourceTerminalType;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.gp.CommandGene;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.IMutateable;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.ProgramChromosome;
import org.jgap.util.CloneException;
import org.jgap.util.ICloneable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JGAPs Gene implementation of an Pointset Measure for LIMES.
 * @author Klaus Lyko (lyko@informatik.uni-leipzig.de)
 * @author Mohamed Sherif (sherif@informatik.uni-leipzig.de)
 * @version Jul 21, 2016
 */
public class PointSetMeasure extends CommandGene implements IMutateable, ICloneable {
    /** */
    private static final long serialVersionUID = -4901752495126327127L;
    static Logger logger = LoggerFactory.getLogger(PointSetMeasure.class);
    
    // Holds the name of this similarity Measure.
    private String operationName = "geo_hausdorff";
    // Set of all allowed similarity measures. Needed for mutation.
    private Set<String> allowedOperations = new HashSet<String>();
    // mutation setter
    private boolean m_mutateable;

    /**
     * Constructor for similarity measures bound by a threshold.
     *
     * @param opName
     *         Name of the LIMES similarity measure operation (e.g. "hausdorff").
     * @param a_conf
     *         JGAP GPConfiguration.
     * @param a_returnType
     *         The return type of this command.
     * @param a_subReturnType
     *         Specifies the SubReturnType.
     * @param a_mutateable
     *         true: this Commandgene is mutateable, viz. the LIMES
     *         similarity measure might be changed to another one out of the
     *         allowed operations.
     * @throws InvalidConfigurationException  when an invalid value has been passed to a Configuration object
     */
    public PointSetMeasure(String opName, final GPConfiguration a_conf, Class<?> a_returnType, int a_subReturnType,
                           boolean a_mutateable) throws InvalidConfigurationException {
        super(a_conf, 2, a_returnType, a_subReturnType,
                new int[]{ResourceTerminalType.POINTSETPROPPAIR.intValue(),
                        // ResourceTerminalType.SOURCE.intValue(),
                        // ResourceTerminalType.TARGET.intValue(),
                        ResourceTerminalType.THRESHOLD.intValue(),});
        fillOperationSet();
        setOperationName(opName);
        m_mutateable = a_mutateable;
        setNoValidation(false);
    }

    /**
     * Constructor for atomic similarity measures. @FIXME antiquated and not
     * actually used.
     *
     * @param opName
     *         Name of the LIMES similarity measure operation (e.g.
     *         "trigram").
     * @param a_conf
     *         JGAP GPConfiguration.
     * @param a_returnType
     *         The return type of this command.
     * @param a_mutateable
     *         true: this Commandgene is mutateable, viz. the LIMES
     *         similarity measure might be changed to another one out of the
     *         allowed operations.
     * @throws InvalidConfigurationException  when an invalid value has been passed to a Configuration object
     */
    public PointSetMeasure(String opName, final GPConfiguration a_conf, final Class<?> a_returnType, boolean a_mutateable)
            throws InvalidConfigurationException {
        super(a_conf, 2, a_returnType, 1, new int[]{ResourceTerminalType.STRINGPROPPAIR.intValue(),
                ResourceTerminalType.THRESHOLD.intValue(),});
        fillOperationSet();
        setOperationName(opName);
        m_mutateable = a_mutateable;
        setNoValidation(false);
    }

    /**
     * Basic constructor for an atomic similarity measure. Return Type will be
     * set to String.class and this gene more precisely the similarity measure
     * command will not be mutateable. @FIXME antiquated and not actually used
     *
     * @param opName
     *         name of the LIMES similarity measure operation (e.g.
     *         "trigram").
     * @param a_conf
     *         JGAP GPConfiguration.
     * @throws InvalidConfigurationException  when an invalid value has been passed to a Configuration object
     */
    public PointSetMeasure(String opName, final GPConfiguration a_conf) throws InvalidConfigurationException {
        this(opName, a_conf, String.class, false);
    }

    @Override
    public String toString() {
        return operationName + "(&1)|&2";
    }

    /**
     * We expect the first 2 parameters to be resource identifier, the third to
     * be an (optional) double value as the threshold for this Similarity
     * command.
     *
     * @param a_ind
     *         A GPProgram
     * @param a_chromNum
     *         The number of the chromosome.
     * @return Class type of the child.
     */
    public Class<?> getChildType(IGPProgram a_ind, int a_chromNum) {
        if (a_chromNum == 0)
            return PairSimilar.class;
        else
            return CommandGene.DoubleClass;

    }

    public String getName() {
        return getOperationName();
    }

    public String getOperationName() {
        return operationName;
    }

    /**
     * Setter for the operation of this similarity measure.
     * @param opName Name of the measure, e.g. "hausdorff"
     */
    private void setOperationName(String opName) {
        operationName = opName;
        allowedOperations.add(opName);
    }

    /**
     * Executes this CommandGene as an object. It's called if the return type is set
     * to String.class. Thereby returning the atomic LIMES expression
     * <code>"sim(a.resource, b.resource)|threshold"</code>.
     */
    @Override
    public Object execute_object(ProgramChromosome a_chrom, int a_n, Object[] args) {
        double threshold;
        @SuppressWarnings("unchecked")
        PairSimilar<String> propPair = (PairSimilar<String>) a_chrom.execute_object(a_n, 0, args);
        threshold = a_chrom.execute_double(a_n, 1, args);

        LinkSpecGeneticLearnerConfig ExpConfig = (LinkSpecGeneticLearnerConfig) getGPConfiguration();
        StringBuffer value = new StringBuffer(getOperationName());
        value.append("(");
        value.append(ExpConfig.getExpressionProperty("source", propPair.a));
        value.append(",");
        value.append(ExpConfig.getExpressionProperty("target", propPair.b));
        value.append(")");
        value.append("|");
        value.append(new BigDecimal(threshold).setScale(4, BigDecimal.ROUND_HALF_EVEN));
        return value.toString();
    }

    @Override
    public void execute_void(ProgramChromosome a_chrom, int a_n, Object[] args) {
        execute_object(a_chrom, a_n, args);
    }

    /**
     * Fill Set of allowed operations with default values. Which are all LIMES
     * String similarity measures.
     */
    private void fillOperationSet() {
    	ArrayList<String> ops = new ArrayList<String>();
    	ops.add(MeasureFactory.GEO_CENTROID_INDEXED_HAUSDORFF);
    	ops.add(MeasureFactory.GEO_FAST_HAUSDORFF);
    	ops.add(MeasureFactory.GEO_FAIR_SURJECTION);
    	ops.add(MeasureFactory.GEO_FRECHET);
    	ops.add(MeasureFactory.GEO_GREAT_ELLIPTIC);
    	ops.add(MeasureFactory.GEO_HAUSDORFF);
    	ops.add(MeasureFactory.GEO_INDEXED_HAUSDORFF);
    	ops.add(MeasureFactory.GEO_LINK);
    	ops.add(MeasureFactory.GEO_MAX);
    	ops.add(MeasureFactory.GEO_MEAN);
    	ops.add(MeasureFactory.GEO_MIN);
    	ops.add(MeasureFactory.GEO_NAIVE_HAUSDORFF);
    	ops.add(MeasureFactory.GEO_NAIVE_SURJECTION);
    	ops.add(MeasureFactory.GEO_ORTHODROMIC);
    	ops.add(MeasureFactory.GEO_SCAN_INDEXED_HAUSDORFF);
    	ops.add(MeasureFactory.GEO_SUM_OF_MIN);
    	ops.add(MeasureFactory.GEO_SYMMETRIC_HAUSDORFF);    	
    	allowedOperations.addAll(ops);
    }

    @Override
    public CommandGene applyMutation(int a_index, double a_percentage) throws InvalidConfigurationException {
        // we will change the measure to a random one out of the Set of allowed
        // operations
        if (!m_mutateable) {
            return this;
        }
        RandomGenerator randomGen = getGPConfiguration().getRandomGenerator();
        double random = randomGen.nextDouble();
        if (random < a_percentage) {
            return applyMutation();
        }
        return this;
    }

    /**
     * Mutates this CommandGene. A random command out of the set of allowed
     * similarity measures is picked.
     *
     * @return A random command out of the set of allowed similarity measures
     * @throws InvalidConfigurationException  when an invalid value has been passed to a Configuration object
     */
    public CommandGene applyMutation() throws InvalidConfigurationException {
        String[] aO = {};
        aO = allowedOperations.toArray(aO);
        RandomGenerator randomGen = getGPConfiguration().getRandomGenerator();
        String newOp = aO[randomGen.nextInt(aO.length)];
        StringMeasure result = new StringMeasure(newOp, getGPConfiguration(), getReturnType(), getSubReturnType(),
                m_mutateable);
        return result;
    }

    @Override
    public Object clone() {
        try {
            PointSetMeasure result = new PointSetMeasure(operationName, getGPConfiguration(), getReturnType(),
                    getSubReturnType(), m_mutateable);
            return result;
        } catch (Throwable t) {
            throw new CloneException(t);
        }
    }

    @Override
    public boolean isValid(ProgramChromosome a_program, int a_index) {
        Object[] o = new Object[0];
        LinkSpecGeneticLearnerConfig expConfig = (LinkSpecGeneticLearnerConfig) getGPConfiguration();
        PairSimilar<?> propPair = (PairSimilar<?>) a_program.execute_object(a_index, 0, o);
        return expConfig.getPropertyMapping().isMatch(propPair.a.toString(), propPair.b.toString());
    }
    
    @Override
    public boolean isValid(ProgramChromosome a_program) {
        return isValid(a_program, 0);
    }
    /**
     * Set possible operations, used while mutating.
     * @param operationSet
     */
    public void setAllowedOperations(Set<String> operationSet) {
    	this.allowedOperations = operationSet;
    }
}
