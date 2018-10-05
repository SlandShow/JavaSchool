package com.slandshow.service.Impl;

import com.slandshow.DAO.MappingGraphDAO;
import com.slandshow.DTO.EdgeDTO;
import com.slandshow.DTO.ScheduleDTO;
import com.slandshow.models.MappingEdge;
import com.slandshow.models.Schedule;
import com.slandshow.service.GraphService;
import com.slandshow.service.ScheduleService;
import com.slandshow.utils.Algorithms.Graph.Graph;
import com.slandshow.utils.Algorithms.GraphExecuter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GraphServiceImpl implements GraphService {

    private static final Graph<String> GRAPH_MODEL = new Graph<String>(false);

    private static final Logger LOGGER = Logger.getLogger(GraphServiceImpl.class);

    @Autowired
    private MappingGraphDAO mappingGraphDAO;

    @Autowired
    private ScheduleService scheduleService;

    @Transactional
    public void addEdge(MappingEdge edge) {
        mappingGraphDAO.add(edge);

        GRAPH_MODEL.addEdge(
                edge.getStationStart().getName(),
                edge.getStationEnd().getName(),
                edge.getRangeDistance()
        );

        LOGGER.info(
                                "NEW EDGE ADDED TO DATABASE AND GRAPH: "
                                + edge.getStationStart() + " -> " + edge.getStationEnd()
                                + " in distance " + edge.getRangeDistance()
        );
    }

    @Transactional
    public void buildGraph() {
        List<MappingEdge> allEdges = mappingGraphDAO.getAllEdges();

        for (MappingEdge currentEdge: allEdges) {
            EdgeDTO edgeDTO = new EdgeDTO();
            edgeDTO.setStationStart(currentEdge.getStationStart().getName().intern());
            edgeDTO.setStationEnd(currentEdge.getStationEnd().getName().intern());
            edgeDTO.setBranch(currentEdge.getBranch().getName().intern());
            edgeDTO.setRangeDistance(currentEdge.getRangeDistance());

            GRAPH_MODEL.addEdge(
                                edgeDTO.getStationStart(),
                                edgeDTO.getStationEnd(),
                                edgeDTO.getRangeDistance()
            );
        }

        LOGGER.info("BUILD GRAPH: \n" + GRAPH_MODEL.toString());
    }

    public void deleteEdge(EdgeDTO edgeDTO) {

    }

    public List<String> searchEdges(String start, String end) {
        return GraphExecuter.shortestUnweightedPath(
                GRAPH_MODEL,
                start,
                end
        );
    }

    @Transactional
    public List<EdgeDTO> getAllEdges() {
        return mappingGraphDAO.getAllEdges();
    }

    public String[] parsePath(List<String> path) {
        if (path.size() < 2)
            return null;

        String[] validPath = new String[path.size() - 1];

        for (int i = 0; i < path.size() - 1; i++) {
            validPath[i] = path.get(i) + " " + path.get(i + 1);
            LOGGER.info("PARSED PATH IS " + validPath[i]);
        }

        return validPath;
    }

    public List<Schedule> puzzleSchedules(String[] path, String dateDeparture, String dateArrival) throws ParseException {
        List<Schedule> puzzled = new ArrayList<>();

        for (int i = 0; i < path.length; i++) {
            ScheduleDTO schedule = new ScheduleDTO();
            schedule.setStationDepartureName(path[i].split(" ")[0]);
            schedule.setStationArrivalName(path[i].split(" ")[1]);
            schedule.setDateDeparture(dateDeparture);
            schedule.setDateArrival(dateArrival);



            Schedule realSchedule = scheduleService.mapping(schedule);

            if (realSchedule.getDateArrival() != null) {
                List<Schedule> tmp = scheduleService.getByStationsViaDates(realSchedule);
                puzzled.addAll(tmp);
            } else {
                List<Schedule> tmp = scheduleService.getByStationsViaDate(realSchedule);
                puzzled.addAll(tmp);
            }
        }

        //filterPuzzledSchedule(puzzled);

        return puzzled;
    }

    public List<List<Schedule>> getValidPazzledSchedulers(List<Schedule> schedules) {
        List<List<Schedule>> puzzled = new ArrayList<>();

        for (Schedule iterator: schedules) {

        }
        return null;
    }

    public void filterPuzzledSchedule(List<Schedule> schedules) {
        schedules = schedules.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private EdgeDTO mapping() {
        return null;
    }
}
