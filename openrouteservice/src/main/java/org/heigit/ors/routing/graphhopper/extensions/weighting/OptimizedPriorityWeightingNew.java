/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library; 
 *  if not, see <https://www.gnu.org/licenses/>.  
 */
package org.heigit.ors.routing.graphhopper.extensions.weighting;

import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.PriorityCode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;

import static com.graphhopper.routing.util.EncodingManager.getKey;

public class OptimizedPriorityWeightingNew extends FastestWeighting {
	private static final double PRIORITY_BEST = PriorityCode.BEST.getValue();
	private static final double PRIORITY_UNCHANGED = PriorityCode.UNCHANGED.getValue();
	private final DecimalEncodedValue priorityEncoder;

	public OptimizedPriorityWeightingNew(FlagEncoder encoder, PMap map) {
		super(encoder, map);
		priorityEncoder = encoder.getDecimalEncodedValue(getKey(encoder, "priority"));
	}

	@Override
	public double calcWeight(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
		double weight = super.calcWeight(edgeState, reverse, prevOrNextEdgeId);
		if (Double.isInfinite(weight))
			return Double.POSITIVE_INFINITY;

		double priority = priorityEncoder.getDecimal(reverse, edgeState.getFlags());

		double normalizedPriority = priority * PRIORITY_BEST - PRIORITY_UNCHANGED;

		double factor = Math.pow(2, normalizedPriority / (PRIORITY_UNCHANGED - PRIORITY_BEST));

		return weight * factor;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final OptimizedPriorityWeightingNew other = (OptimizedPriorityWeightingNew) obj;
		return toString().equals(other.toString());
	}

	@Override
	public int hashCode() {
		return ("OptimizedPriorityWeightingNew" + toString()).hashCode();
	}

	@Override
	public String getName() {
		return "recommendednew";
	}
}
