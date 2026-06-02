import math

cx, cy = 54, 54
R = 32
r = 14

# Red's tangency line: 
# Point on inner circle: T0 = (54, 40)
# Angle of T0 from center is 270 deg (or -90 deg - straight up).
# Tangency line goes RIGHT.
# Let's say angle is in radians. 
a0 = -math.pi / 2
T0x, T0y = cx + r*math.cos(a0), cy + r*math.sin(a0)

dx = math.sqrt(R**2 - r**2)
P0x, P0y = T0x + dx, T0y # (82.775, 40)

# Red will arc from (82.775, 40) counter-clockwise.
# How far? Exactly 120 degrees! Because it must end where the previous slice's tangency line hits the outer circle.
# Previous slice is Green. Green is rotated -120 deg (counter-clockwise) from Red.
# So its outer point is P0 rotated by -120 deg around center.

a1 = math.atan2(P0y - cy, P0x - cx)
a2 = a1 - 2*math.pi/3

P1x = cx + R * math.cos(a2)
P1y = cy + R * math.sin(a2)

# Now, we go from P1 to the inner circle!
# Where does it touch the inner circle? That is the tangency point of Green!
# Green's tangency point is T0 rotated -120 deg.
a3 = a0 - 2*math.pi/3
T1x, T1y = cx + r * math.cos(a3), cy + r * math.sin(a3)

print("Red Polygon:")
print(f"M {T0x:.3f},{T0y:.3f}")
print(f"L {P0x:.3f},{P0y:.3f}")
print(f"A {R},{R} 0 0,0 {P1x:.3f},{P1y:.3f}")
print(f"L {T1x:.3f},{T1y:.3f}")
print(f"A {r},{r} 0 0,1 {T0x:.3f},{T0y:.3f}")
print("Z")
