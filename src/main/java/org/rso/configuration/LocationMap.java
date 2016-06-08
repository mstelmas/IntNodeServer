package org.rso.configuration;


public class LocationMap {

//    private final AppProperty appProperty = AppProperty.getInstance();
//
//    private final Map<Location, List<NodeInfo>> locationMap = new HashMap<>();
//
//    public void addEntry(final Location location, final NodeInfo nodeInfo) {
//
//        // TODO: Duplicates
//        if(locationMap.containsKey(location)){
//            locationMap.get(location).add(nodeInfo);
//        } else {
//            final List<NodeInfo> nodeInfos = new ArrayList<>();
//            nodeInfos.add(nodeInfo);
//            locationMap.put(location, nodeInfos);
//        }
//    }
//
//    public List<NodeInfo> getNodesByLocation(final Location location){
//        return Optional.ofNullable(locationMap.get(location))
//                .orElse(Collections.emptyList());
//    }
//
//    public void removeNodeInfo(final NodeInfo nodeInfo) {
//        for (Location location: locationMap.keySet()){
//            List<NodeInfo> nodeInfos = locationMap.get(location);
//            nodeInfos.remove(nodeInfo);
//        }
//    }
//
//    public boolean hasDataByLocation(Location location){
//        return getNodesByLocation(location).contains(appProperty.getSelfNode());
//    }
//
//    public Map<Location, List<NodeInfo>> getLocationMap() {
//        return ImmutableMap.copyOf(locationMap);
//    }
//
//    public List<Location> getLocationsForNode(@NonNull final NodeInfo nodeInfo) {
//        return locationMap.entrySet().stream()
//                .filter(locationListEntry -> locationListEntry.getValue().contains(nodeInfo))
//                .map(Map.Entry::getKey)
//                .collect(toList());
//    }
}
