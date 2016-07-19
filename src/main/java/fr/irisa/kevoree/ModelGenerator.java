package fr.irisa.kevoree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.pmodeling.api.json.JSONModelSerializer;

import fr.mleduc.poc.graph.generator.graph.Graph;
import fr.mleduc.poc.graph.generator.operations.IOperation;
import fr.mleduc.poc.graph.generator.service.GraphService;
import fr.mleduc.poc.graph.generator.service.OperationsService;
import fr.mleduc.poc.graph.generator.service.output.KevPrinterService;
import fr.mleduc.poc.graph.generator.service.output.KevoreeModelService;

public class ModelGenerator {
	private static JSONModelSerializer jsonSerializer = new DefaultKevoreeFactory().createJSONSerializer();

	public static void generateModel(final String[] args, int numberOfNodes, int numberOfComponents, int numberOfChannels) throws FileNotFoundException, IOException {

		// final long seed = -3309530520489196191L;
		final long seed = initSeed(args);

		final Random generator = new Random();
		generator.setSeed(seed);

		//final int n = 4;
		final OperationsService graphService = new OperationsService(generator).withNodes(numberOfNodes).withComponents(numberOfComponents)
				.withChannels(numberOfChannels).withGroups(1);

		final List<IOperation> operations = graphService.initialize();

		final Graph graph = saveAll(operations, 0, null);

		final List<IOperation> operations2 = graphService.next(graph);
		saveAll(operations2, 1, graph);
	}

	private static Graph saveAll(final List<IOperation> operations, final int step, Graph graph0)
			throws IOException, FileNotFoundException {
		final String kevs1 = new KevPrinterService().process(operations);
		IOUtils.write(kevs1, new FileOutputStream(new File("model" + step + ".kevs")), Charset.defaultCharset());

		GraphService graphService;
		if (graph0 != null) {
			graphService = new GraphService(graph0);
		} else {
			graphService = new GraphService();
		}
		final Graph graph = graphService.process(operations);
		final ContainerRoot model = new KevoreeModelService().process(graph);
		final String jsonModel = jsonSerializer.serialize(model);
		IOUtils.write(jsonModel, new FileOutputStream(new File("model" + step + ".json")), Charset.defaultCharset());
		return graph;
	}

	private static long initSeed(final String[] args) {
		long seed;
		if ((args.length > 0) && StringUtils.isNotBlank(args[0])) {
			try {
				seed = Long.parseLong(args[0]);
				System.out.println("Using user defined " + seed + " as the random generator seed");
			} catch (final NumberFormatException e) {
				seed = -1;
				System.out.println("User defined seed " + args[0] + "is not a valid long");
				System.exit(-1);
			}
		} else {
			seed = new Random().nextLong();
			System.out.println("Using randomly generated " + seed + " as the random generator seed");
		}
		return seed;
	}
}
