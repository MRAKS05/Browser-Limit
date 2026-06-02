import math

cx, cy = 54, 54
R = 32
r = 14

dx = math.sqrt(R*R - r*r)
P1_start = (cx, cy - r)
P1_end = (cx + dx, cy - r)

print(f"Red start: {P1_start}, Red end tangent: {P1_end}")

a1 = math.atan2(-r, dx)
print(f"Angle of P1_end: {math.degrees(a1)}")

# Red is from P1_start to P1_end. Then we arc along outer circle.
# Up to where? The next tangent line starts at rotation -120 deg.
# Actually, the tangency line from the next slice defines the boundary.
# The next tangency line starts at (cx + r*cos(30), cy + r*sin(30)) = (54+12.12, 54+7).
# Which goes towards the outer circle.
# Let's just output overlapping polygons! A polygon that starts at (54,40), goes right to (82.775, 40).
# Then it just goes WAY outside the circle to the left, (0, 0), down to (54, 54).
# If we rotate this shape, and they are drawn sequentially, they might overlap correctly!
# Let's think:
# Red slice starts at (54, 40) -> (100, 40) [far outside] -> (-20, 0) -> (54, 54) -> close.
# If we clip it, it creates the perfect shape!
# Wait, if red draws, then Yellow rotates 120 and draws, it will OVERWRITE part of Red!
# The tangency line of Red is at top, going right. So Yellow (rotated 120 deg clockwise) will have its tangency line going bottom-left.
# The region Yellow overwrites should be exactly what cuts Red off!
# If we do this, the last drawn (Green) will cut Yellow. But then Red needs to cut Green!
# Since Red was drawn first, Green would overlap Red's tangency line.
# So Red would need to be drawn AGAIN (just the part that covers Green) or we just use exact polygons.
